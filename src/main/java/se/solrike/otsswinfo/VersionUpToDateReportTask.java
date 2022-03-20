package se.solrike.otsswinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.artifacts.ComponentMetadata;
import org.gradle.api.artifacts.ComponentSelection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

import groovy.lang.Closure;
import se.solrike.otsswinfo.impl.ArtifactMetadata;
import se.solrike.otsswinfo.impl.CsvVersionUpToDateReportAction;

/**
 * The task will scan all projects runtime dependencies and generate a report with version and if the there is a later
 * version of each dependency.
 * <p>
 * This plugin works best with dependencies that follows https://semver.org semantic versioning.
 *
 * @author Lucas Persson
 */
public abstract class VersionUpToDateReportTask extends OtsSwInfoBaseTask {

  private static final Logger sLogger = LoggerFactory.getLogger(VersionUpToDateReportTask.class);

  /**
   * Closure that takes a version string and return true if it is considered a stable version
   *
   * @return closure for evaluating a version
   */
  @Nested
  @Optional
  public abstract Property<Closure<Boolean>> getIsStable();

  /**
   * Allowed older major version increments compared to current stable version.
   * <p>
   * Default 0.
   *
   * @return the max number of older major version compared to current stable version.
   */
  @Input
  @Optional
  public abstract Property<Integer> getAllowedOldMajorVersion();

  /**
   * Allowed older minor version increments compared to current stable version.
   * <p>
   * Default 2.
   *
   * @return the max number of older minor version compared to current stable version.
   */
  @Input
  @Optional
  public abstract Property<Integer> getAllowedOldMinorVersion();

  /**
   * Map of dependencies that weren't possible to resolve properly.
   * <p>
   * The key in the map is the artifact name in GAV format (group:artifact:version).
   */
  protected Map<String, ArtifactMetadata> mNonDetermineDependencies = new HashMap<>();

  @TaskAction
  void run() {

    initExcludeArtifactGroupsAll();

    scanDependencies();

    setLatestVersion();

    File reportFile = generateUpToDateVersionReport();
    int totalNumberOfDeps = mDependencies.values().size() + mNonDetermineDependencies.values().size();

    long noofOutdateDeps = mDependencies.values().stream().filter(metaData -> !metaData.isLatest()).count();
    getLogger().error("Number of OTS SW that are not the latest: {} out of {}", noofOutdateDeps, totalNumberOfDeps);
    long noofTooOld = mDependencies.values()
        .stream()
        .filter(metaData -> metaData.isTooOldVersion != null && metaData.isTooOldVersion)
        .count();
    getLogger().error("Number of OTS SW that are too old: {} out of {}", noofTooOld, totalNumberOfDeps);
    getLogger().error("See the version up-to-date report at: {}", reportFile.getAbsolutePath());

  }

  private File generateUpToDateVersionReport() {
    ArrayList<ArtifactMetadata> deps = new ArrayList<>(mDependencies.values());
    Collections.sort(deps);
    CsvVersionUpToDateReportAction reportAction = new CsvVersionUpToDateReportAction();
    return reportAction.generateReport(getReportCsvSeparator().getOrElse(","), getReportsDir().getAsFile().get(),
        getExtraVersionInfo().get(), deps, mNonDetermineDependencies.values());

  }

  protected void setLatestVersion() {
    for (ArtifactMetadata metadata : mDependencies.values()) {
      // use Ivy notation with "+" to get the latest
      // but we might end up with an alpha or beta release so those needs to be filtered out
      Dependency query = getProject().getDependencies()
          .create(metadata.artifact.getModuleGroup() + ":" + metadata.artifact.getModuleName() + ":+");

      Configuration latestConfiguration = getProject().getConfigurations().detachedConfiguration(query);

      // configure stable version filter
      configureVersionFilter(latestConfiguration);

      LenientConfiguration lenient = latestConfiguration.getResolvedConfiguration().getLenientConfiguration();

      if (lenient.getFirstLevelModuleDependencies().iterator().hasNext()) {
        ResolvedDependency latest = lenient.getFirstLevelModuleDependencies().iterator().next();
        metadata.latestVersion = latest.getModuleVersion();

        // check how old the current version is
        metadata.isTooOldVersion = isTooOld(getAllowedOldMajorVersion().getOrElse(0),
            getAllowedOldMinorVersion().getOrElse(2), metadata.artifact.getModuleVersion(), metadata.latestVersion);

      }
      else {
        mNonDetermineDependencies.put(metadata.artifactName, metadata);
        sLogger.error("Not possible to determin if {} is latest version or not.", metadata.artifactName);
      }
    }
    for (String gav : mNonDetermineDependencies.keySet()) {
      mDependencies.remove(gav);
    }
  }

  protected static boolean isTooOld(int allowedOldMajorVersion, int allowedOldMinorVersion, String currentVersion,
      String latestVersion) {
    // do semantic versioning comparison
    Semver currentVersionSem = new Semver(currentVersion, SemverType.LOOSE);
    Semver latestVersionSem = new Semver(latestVersion, SemverType.LOOSE);

    boolean isTooOld = (latestVersionSem.getMajor() - currentVersionSem.getMajor()) > allowedOldMajorVersion;
    if (isTooOld) {
      return true;
    }
    else if (latestVersionSem.getMajor().equals(currentVersionSem.getMajor()) && currentVersionSem.getMinor() != null
        && latestVersionSem.getMinor() != null) {
      return ((latestVersionSem.getMinor() - currentVersionSem.getMinor()) > allowedOldMinorVersion);
    }
    return false;
  }

  protected void configureVersionFilter(Configuration configuration) {
    configuration.resolutionStrategy(strategy -> strategy.componentSelection(rules -> rules.all(this::revisionFilter)));
  }

  protected void revisionFilter(ComponentSelection selection) {
    ComponentMetadata metadata = selection.getMetadata();
    // @formatter:off
    boolean accepted = (metadata == null)
        || (metadata.getStatus().equals("release")
            && getIsStable().getOrElse(IsStableDefault.isStable).call(metadata.getId().getVersion()))
        || selection.getCandidate().getVersion().equals("none");
    // @formatter:on
    if (!accepted) {
      // run with -i and this will be printed on the console
      selection.reject("Component status " + metadata.getStatus() + " rejected");
    }
  }

}
