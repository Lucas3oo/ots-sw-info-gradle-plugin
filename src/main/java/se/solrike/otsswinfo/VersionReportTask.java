package se.solrike.otsswinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import se.solrike.otsswinfo.impl.ArtifactMetadata;
import se.solrike.otsswinfo.impl.ArtifactMetadataUtil;
import se.solrike.otsswinfo.impl.CsvVersionReportAction;
import se.solrike.otsswinfo.impl.NewToReleaseHelper;

/**
 * The task will scan all projects runtime dependencies and generate a report with version and licence info.
 *
 * @author Lucas Persson
 */
public abstract class VersionReportTask extends DefaultTask {

  /**
   * Dependencies to exclude from the report. Identified via groupId. e.g. com.example.mylib
   *
   * @return list of dependencies group name to exclude from the report
   */
  @Input
  @Optional
  abstract ListProperty<String> getExcludeArtifactGroups();

  /**
   * Exclude dependencies with same groupId as the project that applies this plugin.
   * <p>
   * Default true.
   *
   * @return true is exclude with same groupId as the project
   */
  @Input
  @Optional
  abstract Property<Boolean> getExcludeOwnGroup();

  /**
   *
   * @return list of sub project names to exclude from the scan
   */
  @Input
  @Optional
  abstract ListProperty<String> getExcludeProjects();

  /**
   * Additional text to add in the beginning of the report
   *
   * @return list of string to add to the report
   */
  @Input
  @Optional
  abstract ListProperty<String> getExtraVersionInfo();

  /**
   * Previous report to compare with in order to set the "new to release" flag.
   *
   * @return the file with the previous release of this project's dependencies.
   */
  @InputFile
  @Optional
  abstract RegularFileProperty getPreviousReportFile();

  /**
   * The directory where reports will be default generated.
   *
   * @return the directory
   */
  @OutputDirectory
  @Optional
  public abstract DirectoryProperty getReportsDir();

  /**
   * In a multiproject it might not be interesting to scan the root project. Default false if the project is a
   * multiproject.
   *
   * @return true if only the root project shall be scanned.
   */
  @Input
  @Optional
  abstract Property<Boolean> getScanRootProject();

  // artifact name as GAV as key
  protected Map<String, ArtifactMetadata> mDependencies = new HashMap<>();

  // list of all groups to exclude plus optional own group
  protected List<String> mExcludeArtifactGroupsAll;

  @TaskAction
  void run() {

    initExcludeArtifactGroupsAll();

    scanDependencies();

    setNewToRelease();

    generateVersionReport();

    getLogger().error("Number of OTS SW: {}", mDependencies.values().size());
  }

  protected void generateVersionReport() {
    ArrayList<ArtifactMetadata> deps = new ArrayList<>(mDependencies.values());
    Collections.sort(deps);
    CsvVersionReportAction reportAction = new CsvVersionReportAction();
    reportAction.generateReport(getReportsDir().getAsFile().get(), getExtraVersionInfo().get(),
        getPreviousReportFile().isPresent(), deps);
  }

  protected void setNewToRelease() {
    // check which dependencies that are new for the release
    if (getPreviousReportFile().isPresent()) {
      NewToReleaseHelper newToReleaseHelper = new NewToReleaseHelper(getPreviousReportFile().getAsFile().get());
      for (ArtifactMetadata artifactMetadata : mDependencies.values()) {
        artifactMetadata.newToRelease = newToReleaseHelper.isDependecyNewToRelease(artifactMetadata);
      }
    }
  }

  @SuppressWarnings("java:S5411")
  protected void scanDependencies() {
    if (getScanRootProject().getOrElse(getProject().getSubprojects().isEmpty())) {
      collectForRuntimeClasspath(getProject());
    }
    else {
      List<String> excludeProjects = getExcludeProjects().get();
      getProject().getSubprojects().forEach(subProject -> {
        if (!excludeProjects.contains(subProject.getName())) {
          collectForRuntimeClasspath(subProject);
        }
      });
    }
  }

  @SuppressWarnings("java:S5411")
  protected void initExcludeArtifactGroupsAll() {
    mExcludeArtifactGroupsAll = getExcludeArtifactGroups().get();
    if (getExcludeOwnGroup().getOrElse(true) && getProject().getGroup() != null) {
      mExcludeArtifactGroupsAll = new ArrayList<>(mExcludeArtifactGroupsAll);
      mExcludeArtifactGroupsAll.add(getProject().getGroup().toString());
    }
  }

  /**
   * Only collect dependencies if it is a Java project. I.e has the java plugin applied.
   *
   * @param project
   *          the gradle multiproject or subproject
   */
  protected void collectForRuntimeClasspath(Project project) {
    boolean hasJavaPlugin = project.getPlugins().hasPlugin(JavaBasePlugin.class);
    if (hasJavaPlugin) {
      project.getConfigurations()
          .getByName("runtimeClasspath")
          .getResolvedConfiguration()
          .getFirstLevelModuleDependencies()
          .forEach(dep -> collectDependencies(project, dep));
    }
  }

  /**
   * Collect dependencies recursive
   *
   * @param project
   *          the gradle multiproject or subproject
   * @param resolvedDependency
   *          resolved dependency
   */
  @SuppressWarnings("java:S5411")
  protected void collectDependencies(Project project, ResolvedDependency resolvedDependency) {

    resolvedDependency.getChildren().forEach(dep -> collectDependencies(project, dep));

    if (!resolvedDependency.getModuleGroup().equals(project.getName())
        && !mExcludeArtifactGroupsAll.contains(resolvedDependency.getModuleGroup())
        && !mDependencies.containsKey(resolvedDependency.getName())) {
      mDependencies.put(resolvedDependency.getName(), buildArtifactMetadata(project, resolvedDependency));
    }

  }

  /**
   * Read any license info, description and project URL in the Maven artifact pom.
   * <p>
   * If license is missing the check the parent's license.
   * <p>
   * If the URL is missing check the parent.
   * <p>
   * Description is only taken from the artifact.
   *
   * @param project
   *          multiproject or subproject
   * @param artifact
   *          resolved dependency
   *
   * @return artifact meta data with GAV and other info like license text and project URL
   */
  protected ArtifactMetadata buildArtifactMetadata(Project project, ResolvedDependency artifact) {
    ArtifactMetadata metadata = new ArtifactMetadata(artifact.getName(), artifact);
    return ArtifactMetadataUtil.updateArtifactMetadataRecursive(project, artifact.getName(), metadata);
  }

}
