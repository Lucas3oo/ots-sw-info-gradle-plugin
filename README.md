# ots-sw-info-gradle-plugin
Gradle plugin to generate version and license reports for your Java (multi)project.
The plugin scans all the dependencies and the transitive dependencies (off the shelf software, OTS-SW)
and lists the version and license for each dependency.

The plugin can also generate an version up-to-date report for the dependencies.


## Usage

### Apply to your project

Apply the plugin to your project.
Refer [the Gradle Plugin portal](https://plugins.gradle.org/plugin/se.solrike.otsswinfo) about the detail
of installation procedure.

Gradle 7.0 or later must be used.

### How it works
If the project is a multiproject then all of the Java subprojects will be scanned.

If the subprojects in the multiproject has inter dependencies those will not be included in the report if they have the
same groupId as the multiproject.

The reports are CSV files that uses tab as separator.

There are two tasks; one `versionReport` that generates a report on the version and license for each dependency by
scanning all the dependencies and the transitive dependencies. The license info is taken from the dependencies pom.xml.
If the info in that is missing the parent POM is used instead until a license info is found.

The other task; `versionUpToDateReport` checks if the used dependency is of latest version or not.
Unfortunately there isn't any reliable info on what versions in Maven repository
that are stable and not a milestone or alpha/beta build. So some rules is implemented in this plugin to filter out the
unstable versions. For instance if the version contains any of the strings final, release or GA then it is consider
a stable release. If the version follows the semantic versioning `<major>.<minor>.<patch>` it is also consider as stable.
Some dependencies like `com.google.guava:guava:31.0.1-jre` doesn't follow that rules so here this plugin will give a
false positive since it will think version `23.0` is the latest stable version of guava.
Some dependencies lacks proper meta data, like `commons-codec:commons-codec:1.15` then the latest version will be `null`.
A warning is also printed on the console when executing the report task.


### Configure OTS SW info Plugin

Configure the `otsSwInfo` extension to use the tasks `versionReport` and `versionUpToDateReport`.

```groovy
otsSwInfo {
  excludeArtifactGroups = ['com.example.mylib']
  excludeOwnGroup = true // default true id the project has the group property set
  excludeProjects = ['my-cool-component-api','integration-testframework']
  extraVersionInfo = ["Version description for $project.name $releaseVersion"]
  // if an old report is submitted a "new to release" column is added to the version and license report.
  previousReportFile = layout.projectDirectory.file('config/versionReport/MyProduct_1_0_JavaVersionAndLicenseReport.csv')
  reportsDir = 'someFolder' // default build/reports/otsswinfo
  scanRootProject = false // default false if the project is a multiproject
}

otsSwInfo {
  excludeProjects = ['data-platform-api','integration-testframework']
  excludeArtifactGroups = ['com.cepheid.nexus']
  extraVersionInfo = ["Version description for $project.name $releaseVersion"]
  previousReportFile = layout.projectDirectory.file('config/dependencyReport/Admin1.3_DependencyReport.csv')
}


```




