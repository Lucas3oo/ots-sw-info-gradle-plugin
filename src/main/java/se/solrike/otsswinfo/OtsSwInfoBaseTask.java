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
   * In a multiproject it might not be interesting to scan the root project. Default false if the project is a
   * multiproject.
   *
   * @return true if only the root project shall be scanned.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getScanRootProject();

  /**
   * THe key in the map is the artifact name in GAV format (group:artifact:version).
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
