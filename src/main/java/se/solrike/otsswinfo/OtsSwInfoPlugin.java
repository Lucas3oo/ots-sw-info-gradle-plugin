package se.solrike.otsswinfo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.TaskProvider;
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

    TaskProvider<VersionReportTask> versionReportTask = project.getTasks()
        .register("versionReport", VersionReportTask.class, task -> {
          task.setDescription("Generate a version report for all the dependecies including trasitive dependencies.");
        });

    updateTask(extension, versionReportTask);

    TaskProvider<VersionUpToDateReportTask> versionUpToDateTask = project.getTasks()
        .register("versionUpToDateReport", VersionUpToDateReportTask.class, task -> {
          task.setDescription(
              "Generate a version up-to-date report for all the dependecies including trasitive dependencies.");
        });
    updateTask(extension, versionUpToDateTask);
  }

  protected OtsSwInfoExtension createExtension(Project project, DirectoryProperty reportsBaseDir) {
    OtsSwInfoExtension extension = project.getExtensions().create(EXTENSION_NAME, OtsSwInfoExtension.class);
    DirectoryProperty reportsDirectory = project.getObjects()
        .directoryProperty()
        .convention(reportsBaseDir.map(d -> d.dir(REPORTS_SUBDIR)));
    extension.getReportsDir().set(reportsDirectory);
    return extension;
  }

  protected TaskProvider<? extends VersionReportTask> updateTask(OtsSwInfoExtension extension,
      TaskProvider<? extends VersionReportTask> taskProvider) {
    taskProvider.get().setGroup("Reports");

    taskProvider.get().getExcludeArtifactGroups().set(extension.getExcludeArtifactGroups());
    taskProvider.get().getExcludeOwnGroup().set(extension.getExcludeOwnGroup());
    taskProvider.get().getExcludeProjects().set(extension.getExcludeProjects());
    taskProvider.get().getExtraVersionInfo().set(extension.getExtraVersionInfo());
    taskProvider.get().getPreviousReportFile().set(extension.getPreviousReportFile());
    taskProvider.get().getReportsDir().set(extension.getReportsDir());
    taskProvider.get().getScanRootProject().set(extension.getScanRootProject());

    return taskProvider;
  }

  protected void verifyGradleVersion(GradleVersion version) {
    if (version.compareTo(SUPPORTED_VERSION) < 0) {
      String message = String.format("Gradle version %s is unsupported. Please use %s or later.", version,
          SUPPORTED_VERSION);
      throw new IllegalArgumentException(message);
    }
  }
}
