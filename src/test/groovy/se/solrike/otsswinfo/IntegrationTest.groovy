package se.solrike.otsswinfo

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

/**
 * @author Lucas Persson
 */
class IntegrationTest extends Specification {
  @TempDir
  private File mProjectDir

  private getBuildFile() {
    new File(mProjectDir, "build.gradle")
  }

  private getSettingsFile() {
    new File(mProjectDir, "settings.gradle")
  }

  def 'setup'() {
    settingsFile << ""
    buildFile << """
plugins {
  id ('java-library')
  id('se.solrike.otsswinfo')
}

repositories {
  mavenCentral()
}

dependencies {
  api 'org.springframework:spring-core:5.3.5'
  implementation 'org.slf4j:slf4j-api:1.7.30'
}

group = 'com.example.mylib'
version = '1.0.0'
sourceCompatibility = '11'
"""
  }

  def "can run versionReport task"() {
    given: "build file as in setup"

    when: "exeute the task"
    def result = runGradle(true, List.of("versionReport"));

    then: "the build shall be successful"
    result.task(':versionReport').outcome == SUCCESS
    and: "the report task shall find 3 dependencies since org.springframework:spring-core:5.3.5 depends on org.springframework:spring-jcl:5.3.5"
    result.output.contains("Number of OTS SW: 3")
  }

  def "can run versionUpToDateReport task"() {
    given: "build file as in setup"

    when: "exeute the task"
    def result = runGradle(true, List.of("versionUpToDateReport"));

    then: "the build shall be successful"
    result.task(':versionUpToDateReport').outcome == SUCCESS
    and: "the report shall find 3 dependencies outdated since all are using old versions"
    result.output.contains("Number of OTS SW that are outdated: 3 out of 3")
  }


  BuildResult runGradle(List<String> args) {
    return runGradle(true, args);
  }

  BuildResult runGradle(boolean isSuccessExpected, List<String> args) {
    GradleRunner gradleRunner = GradleRunner.create()
        .withDebug(true)
        .withArguments(args)
        .withProjectDir(mProjectDir)
        .withPluginClasspath(); // to get plugin under test found by the runner
    return isSuccessExpected ? gradleRunner.build() : gradleRunner.buildAndFail();
  }
}
