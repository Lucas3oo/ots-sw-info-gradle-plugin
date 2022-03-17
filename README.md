# ots-sw-info-gradle-plugin
Gradle plugin to generate version and license reports for your Java (multi)project.
The plugin scans all the dependencies and the transitive dependencies (off the shelf software, OTS-SW)
and lists the version and license for each dependency.

The plugin can also generate an version up-to-date report for the dependencies.

And the plugin can also check the license on each dependency. Default
[popular open source licenses](https://opensource.org/licenses) will not give a
warning but licenses like [AGPL](https://opensource.org/licenses/AGPL-3.0) that are problematic to use from commercial
software will optional fail the build.

This plugin works best with dependencies that follows https://semver.org semantic versioning and have proper info
in the POM xml in the Maven repo.


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

There are two report tasks; one `versionReport` that generates a report on the version and license for each dependency by
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

The license check task; `licenseCheck` checks all dependencies licenses if they are allowed or not. If the license name is in any of the four pre-defined [files](./src/main/resources/se/solrike/otsswinfo/impl/) then it is considered to be allowed.

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
```

If the algorithm to determine what versions are to be considered stable needs to be customised then this kind of
closure can be configured:

```groovy
def isStableCheck = { String version ->
  def stableKeyword = ['JRE'].any { it -> version.toUpperCase().contains(it) }
  return stableKeyword || se.solrike.otsswinfo.IsStableDefault.isStable(version)
}
otsSwInfo {
  // customize what is considered a stable version
  isStable = isStableCheck
}
```

Configure the `otsSwInfo` extension to use the task `licenseCheck`.

```groovy
otsSwInfo {
  excludeArtifactGroups = ['com.example.mylib']
  excludeOwnGroup = true // default true id the project has the group property set
  excludeProjects = ['my-cool-component-api','integration-testframework']
  scanRootProject = false // default false if the project is a multiproject
  // optionally override the file with permissiveLicenses
  // the task simply checks the license text if it is present in the file.
  permissiveLicenses = layout.projectDirectory.file('permissiveLicenses.txt')
  // explicit disallow some licenses that are default allowed by the plugin
  disallowedLicenses = ['GNU General Public License', 'GPL-1.0', 'GPL-1.0+', 'GPL-2.0', 'GPL-2.0+', 'GPL-3.0', 'GPL-3.0+']
  ignoreFailures = true // don't fail the build if disallowed license are used, default false
}
```

## Sample reports
### Version and license report in CSV

|Name|Version|Package Name|Manufacturer URL|Description|License|
|-----|-----|-----|-----|-----|-----|
|slf4j-api|1.7.30|org.slf4j|http://www.slf4j.org|The slf4j API|MIT License|
|spring-aop|5.3.5|org.springframework|https://github.com/spring-projects/spring-framework|Spring AOP|Apache License, Version 2.0|


### Version up-to-date report in CSV

|Name|Version|Package Name|Latest|
|-----|-----|-----|-----|
|slf4j-api|1.7.30|org.slf4j|No - 1.7.36|
|spring-aop|5.3.5|org.springframework|No - 5.3.16|



## Allowed licenses
The list of allowed licenses are in four files:

* [Permissive](./src/main/resources/se/solrike/otsswinfo/impl/permissiveLicenses.txt)
* [Weak copyleft](./src/main/resources/se/solrike/otsswinfo/impl/weakCopyLeftLicenses.txt)
* [Strong copyleft](./src/main/resources/se/solrike/otsswinfo/impl/strongCopyLeftLicenses.txt)
* [GNU](./src/main/resources/se/solrike/otsswinfo/impl/gnuLicenses.txt)

They are in general fine to use if the software is hosted and not distributed.

## Release notes
### 1.0.0-beta.6
* Added some more alias for MIT and LGPL licenses
* Fixed the reports to not print null in case the info is missing.

### 1.0.0-beta.5
* Make the report generation from Maven info more robust against missing info.

### 1.0.0-beta.4
* Change the CSV format on the version up-to-date report to be CSV with padding so it is viewable on the console too.

### 1.0.0-beta.3
* It is possible to specify the separator character for CSV files.

### 1.0.0-beta.2
* Externalise the algorithm to determine the format of a stable release.
* Add licenseCheck task

