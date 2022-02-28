package se.solrike.otsswinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gradle.api.artifacts.ComponentMetadata;
import org.gradle.api.artifacts.ComponentSelection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.solrike.otsswinfo.impl.ArtifactMetadata;
import se.solrike.otsswinfo.impl.CsvVersionUpToDateReportAction;

/**
 * The task will scan all projects runtime dependencies and generate a report with version and if the there is a later
 * version of each dependency.
 * <p>
 * This plugin works best with dependencies that follows semver.org semantic versioning.
 *
 * @author Lucas Persson
 */
public abstract class VersionUpToDateReportTask extends VersionReportTask {

  private static final Logger sLogger = LoggerFactory.getLogger(VersionUpToDateReportTask.class);

  @TaskAction
  @Override
  void run() {

    initExcludeArtifactGroupsAll();

    scanDependencies();

    setLatestVersion();

    generateUpToDateVersionReport();

    long noofOutdateDeps = mDependencies.values().stream().filter(metaData -> !metaData.isLatest()).count();
    getLogger().error("Number of OTS SW that are outdated: {} out of {}", noofOutdateDeps,
        mDependencies.values().size());

  }

  private void generateUpToDateVersionReport() {
    ArrayList<ArtifactMetadata> deps = new ArrayList<>(mDependencies.values());
    Collections.sort(deps);
    CsvVersionUpToDateReportAction reportAction = new CsvVersionUpToDateReportAction();
    reportAction.generateReport(getReportsDir().getAsFile().get(), getExtraVersionInfo().get(), deps);

  }

  protected void setLatestVersion() {
    for (ArtifactMetadata metadata : mDependencies.values()) {

      // use Ivy notation with "+" to get the latest
      // but we might end up with an alpha or beta release so those needs to be filtered out
      Dependency query = getProject().getDependencies()
          .create(metadata.artifact.getModuleGroup() + ":" + metadata.artifact.getModuleName() + ":+");

      Configuration latestConfiguration = getProject().getConfigurations().detachedConfiguration(query);

      addRevisionFilter(latestConfiguration);

      LenientConfiguration lenient = latestConfiguration.getResolvedConfiguration().getLenientConfiguration();

      if (lenient.getFirstLevelModuleDependencies().iterator().hasNext()) {
        ResolvedDependency latest = lenient.getFirstLevelModuleDependencies().iterator().next();
        metadata.latestVersion = latest.getModuleVersion();
      }
      else {
        sLogger.error("Not possible to determin if {} is latest version or not", metadata.artifactName);
      }

    }
  }

  protected void addRevisionFilter(Configuration configuration) {
    configuration.resolutionStrategy(
        strategy -> strategy.componentSelection(rules -> rules.all(VersionUpToDateReportTask::revisionFilter)));
  }

  protected static void revisionFilter(ComponentSelection selection) {
    ComponentMetadata metadata = selection.getMetadata();
    // @formatter:off
    boolean accepted = (metadata == null)
        || (metadata.getStatus().equals("release") && isStable(metadata.getId().getVersion()))
        || selection.getCandidate().getVersion().equals("none");
    // @formatter:on
    if (!accepted) {
      // run with -i and this will be printed on the console
      selection.reject("Component status " + metadata.getStatus() + " rejected");
    }
  }

  protected static boolean isStable(String version) {
    return stableKeyword(version) || stableVersion(version);
  }

  protected static boolean stableKeyword(String version) {
    return List.of("RELEASE", "FINAL", "GA").stream().anyMatch(keyword -> version.toUpperCase().contains(keyword));
  }

  protected static boolean stableVersion(String version) {
    String regex = "^[0-9,.v-]+(-r)?$";
    return version.matches(regex);
  }

}
