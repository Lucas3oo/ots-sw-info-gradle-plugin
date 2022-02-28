package se.solrike.otsswinfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Lucas Persson
 */
class OtsSwInfoExtensionAndTaskTest {

  private Project mProject;

  @BeforeEach
  public void setup() {
    mProject = ProjectBuilder.builder().build();
    mProject.getPluginManager().apply(JavaBasePlugin.class);
    mProject.getPluginManager().apply(OtsSwInfoPlugin.class);
  }

  @Test
  void projectHasExtension() {
    Object extension = mProject.getExtensions().findByName(OtsSwInfoPlugin.EXTENSION_NAME);

    assertThat(extension).isNotNull();
  }

  @Test
  void projectHasTask() {
    VersionReportTask vTask = (VersionReportTask) mProject.getTasks().getByName("versionReport");
    assertThat(vTask).isNotNull();
    VersionUpToDateReportTask vUpTask = (VersionUpToDateReportTask) mProject.getTasks()
        .getByName("versionUpToDateReport");
    assertThat(vUpTask).isNotNull();

  }

}