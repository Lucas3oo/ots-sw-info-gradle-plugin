package se.solrike.otsswinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IntegrationTest {

  @TempDir
  Path mProjectDir;
  Path mBuildFile;

  @BeforeEach
  void setup() throws IOException {
    mBuildFile = Files.createFile(mProjectDir.resolve("build.gradle"));
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(mBuildFile.toFile()))) {
      writer.write("plugins { id ('java-library') \n  id('se.solrike.otsswinfo')  }\n");
      // @formatter:off
      writer.write(
            "repositories {\n"
          + "  mavenCentral()\n"
          + "}\n");
      writer.write(
            "group = 'com.example.mylib'\n"
          + "version = '1.0.0'\n");
      writer.write(
            "dependencies {\n"
          + "  api 'org.springframework:spring-core:5.3.5'\n"
          + "  implementation 'org.slf4j:slf4j-api:1.7.30'\n"
          + "}\n");
      // @formatter:on
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testVersionReport() throws IOException {
    // given the setup with a build file that depends on two artifacts

    // when versionReport is run
    BuildResult buildResult = runGradle(true, List.of("versionReport"));

    // then the gradle build shall be successful
    assertThat(buildResult.task(":versionReport").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    // and the report shall find 3 dependencies since org.springframework:spring-core:5.3.5
    // depends on org.springframework:spring-jcl:5.3.5
    assertThat(buildResult.getOutput()).contains("Number of OTS SW: 3");

    System.err.println(buildResult.getOutput());

  }

  @Test
  void testVersionUpToDateReport() throws IOException {
    // given the setup with a build file that depends on two artifacts which are both outdated

    // when versionReport is run
    BuildResult buildResult = runGradle(true, List.of("versionUpToDateReport"));

    // then the gradle build shall be successful
    assertThat(buildResult.task(":versionUpToDateReport").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    // and the report shall find 3 dependencies outdated since all are using old versions
    assertThat(buildResult.getOutput()).contains("Number of OTS SW that are outdated: 3 out of 3");

    System.err.println(buildResult.getOutput());

  }

  BuildResult runGradle(List<String> args) {
    return runGradle(true, args);
  }

  BuildResult runGradle(boolean isSuccessExpected, List<String> args) {
    GradleRunner gradleRunner = GradleRunner.create()
        .withDebug(true)
        .withArguments(args)
        .withProjectDir(mProjectDir.toFile())
        .withPluginClasspath(); // to get plugin under test found by the runner
    return isSuccessExpected ? gradleRunner.build() : gradleRunner.buildAndFail();
  }

}
