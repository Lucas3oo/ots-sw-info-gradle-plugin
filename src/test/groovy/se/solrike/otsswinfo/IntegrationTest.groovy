package se.solrike.otsswinfo

import static org.gradle.testkit.runner.TaskOutcome.FAILED
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

  private getPermissiveLicenseFile() {
    new File(mProjectDir, "permissiveLicenses.txt")
  }

  def 'setup'() {
    settingsFile << ""
    buildFile << """
plugins {
  id ('java-library')
  id ('se.solrike.otsswinfo')
}
repositories {
  mavenCentral()
}
group = 'com.example.mylib'
version = '1.0.0'
sourceCompatibility = '11'
"""
  }

  def addDepAndConfig() {
    buildFile << """
dependencies {
  api 'org.springframework:spring-core:5.3.5'
  implementation 'org.slf4j:slf4j-api:1.7.30'
}
otsSwInfo {
  disallowedLicenses = ['MIT License']
}
"""
  }


  def "can run versionReport task"() {
    given: "build file as in setup"
    addDepAndConfig()

    when: "exeute the task"
    def result = runGradle(true, List.of("versionReport"));

    then: "the build shall be successful"
    result.task(':versionReport').outcome == SUCCESS
    and: "the report task shall find 3 dependencies since org.springframework:spring-core:5.3.5 depends on org.springframework:spring-jcl:5.3.5"
    result.output.contains("Number of OTS SW: 3")
  }

  def "can run versionUpToDateReport task"() {
    given: "build file as in setup"
    addDepAndConfig()

    when: "exeute the task"
    def result = runGradle(true, List.of("versionUpToDateReport"));

    then: "the build shall be successful"
    result.task(':versionUpToDateReport').outcome == SUCCESS
    and: "the report shall find 3 dependencies outdated since all are using old versions"
    result.output.contains("Number of OTS SW that are outdated: 3 out of 3")
  }


  def "can run licenseCheck task with disallowed list configured"() {
    given: "build file as in setup"
    addDepAndConfig()

    when: "exeute the task"
    def result = runGradle(false, List.of("licenseCheck"));

    then: "the build shall be successful"
    result.task(':licenseCheck').outcome == FAILED
    and: "one dependecy has disallowed licence"
    result.output.contains("Number of OTS SW with disallowed licenses: 1")
    and: "the disallowed liceses is MIT"
    result.output.contains("Dependency org.slf4j:slf4j-api:1.7.30 has a disallowed license of 'MIT License' from URL: http://www.opensource.org/licenses/mit-license.php")
  }

  def "can run licenseCheck task with custom permissive license file"() {
    given: "build file with custom permissive license file"
    permissiveLicenseFile << """
Eclipse Distribution License - v 1.0
"""
    buildFile << """
dependencies {
  api 'org.glassfish.jaxb:txw2:2.3.5' // has Eclipse Distribution License - v 1.0 license
}

otsSwInfo {
  permissiveLicenses = layout.projectDirectory.file('permissiveLicenses.txt')
}

"""

    when: "exeute the task"
    def result = runGradle(true, List.of("licenseCheck"));

    then: "the build shall be successful"
    result.task(':licenseCheck').outcome == SUCCESS
    and: "all depndecies have allowed licences since the EDL license was added"
    result.output.contains("Number of OTS SW with disallowed licenses: 0")
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
