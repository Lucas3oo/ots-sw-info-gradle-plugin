package se.solrike.otsswinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

import se.solrike.otsswinfo.impl.ArtifactMetadata;
import se.solrike.otsswinfo.impl.ArtifactMetadataUtil;

/**
 *
 * @author Lucas Persson
 */
public abstract class OtsSwInfoBaseTask extends DefaultTask {

  /**
   * Dependencies to exclude from the report. Identified via groupId. e.g. com.example.mylib
   *
   * @return list of dependencies group name to exclude from the report
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExcludeArtifactGroups();

  /**
   * Exclude dependencies with same groupId as the project that applies this plugin.
   * <p>
   * Default true.
   *
   * @return true is exclude with same groupId as the project
   */
  @Input
  @Optional
  public abstract Property<Boolean> getExcludeOwnGroup();

  /**
   *
   * @return list of sub project names to exclude from the scan
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExcludeProjects();

  /**
   * Configurations will be scanned for dependencies.
   * <p>
   * Default the list is empty. But recommendation is to configure with <code>runtimeClasspth</code>.
   * <p>
   * If the project is platform dependent other configuration e.g. <code>win64</code> can be included.
   *
   * @return list of configurations to include in the scan.
   */
  @Input
  public abstract ListProperty<String> getIncludeConfigurations();

  /**
   * Additional text to add in the beginning of the report
   *
   * @return list of string to add to the report
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExtraVersionInfo();

  /**
   * The directory where reports will be default generated.
   *
   * @return the directory
   */
  @OutputDirectory
  @Optional
  public abstract DirectoryProperty getReportsDir();

  /**
   * CSV separator. Default ",".
   *
   * @return CSV separator character
   */
  @Input
  @Optional
  public abstract Property<String> getReportCsvSeparator();

  /**
   * In a multiproject it might not be interesting to scan the root project. Default false if the project is a
   * multiproject.
   *
   * @return true if only the root project shall be scanned.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getScanRootProject();

  /**
   * Additional license info to compliment in case the dependency lacks proper metadata in the POM.
   *
   * @return map with GAV format (group:artifact:version) as key and license as value.
   */
  @Input
  @Optional
  public abstract MapProperty<String, String> getAdditionalLicenseMetadata();

  /**
   * Additional URL info to compliment in case the dependency lacks proper metadata in the POM.
   *
   * @return map with GAV format (group:artifact:version) as key and URL as value.
   */
  @Input
  @Optional
  public abstract MapProperty<String, String> getAdditionalUrlMetadata();

  /**
   * Additional description info to compliment in case the dependency lacks proper metadata in the POM.
   *
   * @return map with GAV format (group:artifact:version) as key and description as value.
   */
  @Input
  @Optional
  public abstract MapProperty<String, String> getAdditionalDescriptionMetadata();

  /**
   * The key in the map is the artifact name in GAV format (group:artifact:version).
   */
  protected Map<String, ArtifactMetadata> mDependencies = new HashMap<>();

  /**
   * List of all groups to exclude plus optional own group
   */
  protected List<String> mExcludeArtifactGroupsAll;

  /**
   * Calculate all artifacts groups to exclude for the report
   */
  @SuppressWarnings("java:S5411")
  protected void initExcludeArtifactGroupsAll() {
    mExcludeArtifactGroupsAll = getExcludeArtifactGroups().get();
    if (getExcludeOwnGroup().getOrElse(true) && getProject().getGroup() != null) {
      mExcludeArtifactGroupsAll = new ArrayList<>(mExcludeArtifactGroupsAll);
      mExcludeArtifactGroupsAll.add(getProject().getGroup().toString());
    }
  }

  /**
   * Scan all included dependencies to collect the info into the {@code mDependencies} map.
   */
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

  /**
   * Only collect dependencies if it is a Java project. I.e has the java plugin applied.
   *
   * @param project
   *          the gradle multiproject or subproject
   */
  protected void collectForRuntimeClasspath(Project project) {
    boolean hasJavaPlugin = project.getPlugins().hasPlugin(JavaBasePlugin.class);
    if (hasJavaPlugin) {
      getIncludeConfigurations().get()
          .forEach(configuration -> project.getConfigurations()
              .getByName(configuration)
              .getResolvedConfiguration()
              .getFirstLevelModuleDependencies()
              .forEach(dep -> collectDependencies(project, dep)));
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
    metadata = ArtifactMetadataUtil.updateArtifactMetadataRecursive(project, artifact.getName(), metadata);

    if (metadata.license == null) {
      metadata.license = getAdditionalLicenseMetadata().get().get(metadata.artifactName);
    }
    if (metadata.url == null) {
      metadata.url = getAdditionalUrlMetadata().get().get(metadata.artifactName);
    }
    if (metadata.description == null || metadata.description.equals("")) {
      metadata.description = getAdditionalDescriptionMetadata().get().get(metadata.artifactName);
    }
    return metadata;
  }

}
