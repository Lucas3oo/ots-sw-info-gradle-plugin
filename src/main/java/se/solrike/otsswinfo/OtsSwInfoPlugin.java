package se.solrike.otsswinfo;

import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.util.GradleVersion;

/**
 * @author Lucas Persson
 */
public class OtsSwInfoPlugin implements Plugin<Project> {

  private static final GradleVersion SUPPORTED_VERSION = GradleVersion.version("7.0");
  public static final String EXTENSION_NAME = "otsSwInfo";
  public static final String REPORTS_SUBDIR = "otsswinfo";

  @Override
  public void apply(Project project) {
    verifyGradleVersion(GradleVersion.current());
    project.getPluginManager().apply(ReportingBasePlugin.class);
    // default reports directory
    DirectoryProperty reportsBaseDir = project.getExtensions().getByType(ReportingExtension.class).getBaseDirectory();

    OtsSwInfoExtension extension = createExtension(project, reportsBaseDir);

    project.getTasks().register("versionReport", VersionReportTask.class, task -> {
      task.setDescription("Generate a version report for all the dependecies including trasitive dependencies.");
      task.getPreviousReportFile().set(extension.getPreviousReportFile());
      updateTask(extension, task);
    });

    project.getTasks().register("versionUpToDateReport", VersionUpToDateReportTask.class, task -> {
      task.setDescription(
          "Generate a version up-to-date report for all the dependecies including trasitive dependencies.");
      task.getIsStable().set(extension.getIsStable());
      task.getAllowedOldMajorVersion().set(extension.getAllowedOldMajorVersion());
      task.getAllowedOldMinorVersion().set(extension.getAllowedOldMinorVersion());
      updateTask(extension, task);
    });

    project.getTasks().register("licenseCheck", LicenseCheckTask.class, task -> {
      task.setDescription("Check dependecies' licenses");
      task.getGnuLicenses().set(extension.getGnuLicenses());
      task.getPermissiveLicenses().set(extension.getPermissiveLicenses());
      task.getStrongCopyLeftLicenses().set(extension.getStrongCopyLeftLicenses());
      task.getWeakCopyLeftLicenses().set(extension.getWeakCopyLeftLicenses());
      task.getAllowedLicenses().set(extension.getAllowedLicenses());
      task.getDisallowedLicenses().set(extension.getDisallowedLicenses());
      task.getIgnoreFailures().set(extension.getIgnoreFailures());
      updateTask(extension, task);
    });

  }

  protected OtsSwInfoExtension createExtension(Project project, DirectoryProperty reportsBaseDir) {
    OtsSwInfoExtension extension = project.getExtensions().create(EXTENSION_NAME, OtsSwInfoExtension.class);
    DirectoryProperty reportsDirectory = project.getObjects()
        .directoryProperty()
        .convention(reportsBaseDir.map(d -> d.dir(REPORTS_SUBDIR)));
    extension.getReportsDir().set(reportsDirectory);
    ListProperty<String> includeConfigurationsConvention = project.getObjects()
        .listProperty(String.class)
        .convention(List.of("runtimeClasspath"));
    extension.getIncludeConfigurations().set(includeConfigurationsConvention);
    return extension;
  }

  /**
   * Set common properties for the task
   *
   * @param extension
   *          the extension to update from
   * @param task
   *          the task to update
   */
  protected void updateTask(OtsSwInfoExtension extension, OtsSwInfoBaseTask task) {
    task.setGroup("Reports");
    task.getExcludeArtifactGroups().set(extension.getExcludeArtifactGroups());
    task.getExcludeOwnGroup().set(extension.getExcludeOwnGroup());
    task.getExcludeProjects().set(extension.getExcludeProjects());
    task.getIncludeConfigurations().set(extension.getIncludeConfigurations());
    task.getExtraVersionInfo().set(extension.getExtraVersionInfo());
    task.getReportsDir().set(extension.getReportsDir());
    task.getReportCsvSeparator().set(extension.getReportCsvSeparator());
    task.getScanRootProject().set(extension.getScanRootProject());
    task.getAdditionalLicenseMetadata().set(extension.getAdditionalLicenseMetadata());
    task.getAdditionalUrlMetadata().set(extension.getAdditionalUrlMetadata());
    task.getAdditionalDescriptionMetadata().set(extension.getAdditionalDescriptionMetadata());
  }

  protected void verifyGradleVersion(GradleVersion version) {
    if (version.compareTo(SUPPORTED_VERSION) < 0) {
      String message = String.format("Gradle version %s is unsupported. Please use %s or later.", version,
          SUPPORTED_VERSION);
      throw new IllegalArgumentException(message);
    }
  }
}
