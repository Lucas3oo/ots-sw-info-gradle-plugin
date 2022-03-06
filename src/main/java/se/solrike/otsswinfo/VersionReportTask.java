package se.solrike.otsswinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import se.solrike.otsswinfo.impl.ArtifactMetadata;
import se.solrike.otsswinfo.impl.CsvVersionReportAction;
import se.solrike.otsswinfo.impl.NewToReleaseHelper;

/**
 * The task will scan all projects runtime dependencies and generate a report with version and licence info.
 *
 * @author Lucas Persson
 */
public abstract class VersionReportTask extends OtsSwInfoBaseTask {

  /**
   * Previous report to compare with in order to set the "new to release" flag.
   *
   * @return the file with the previous release of this project's dependencies.
   */
  @InputFile
  @Optional
  public abstract RegularFileProperty getPreviousReportFile();

  @TaskAction
  void run() {

    initExcludeArtifactGroupsAll();

    scanDependencies();

    setNewToRelease();

    File reportFile = generateVersionReport();

    getLogger().error("Number of OTS SW: {}", mDependencies.values().size());
    getLogger().error("See the version report at: {}", reportFile.getAbsolutePath());
  }

  protected File generateVersionReport() {
    ArrayList<ArtifactMetadata> deps = new ArrayList<>(mDependencies.values());
    Collections.sort(deps);
    CsvVersionReportAction reportAction = new CsvVersionReportAction();
    return reportAction.generateReport(getReportsDir().getAsFile().get(), getExtraVersionInfo().get(),
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

}
