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
As a last resort the plugin can be configured with a map of dependencies and licenses to fill the gaps.

The other task; `versionUpToDateReport` checks if the used dependency is of latest version or not.
Unfortunately there isn't any reliable info on what versions in Maven repository
that are stable and not a milestone or alpha/beta build. So some rules is implemented in this plugin to filter out the
unstable versions. For instance if the version contains any of the strings final, release or GA then it is consider
a stable release. If the version follows the semantic versioning `<major>.<minor>.<patch>` it is also consider as stable.
Some dependencies like `com.google.guava:guava:31.0.1-jre` doesn't follow that rules so here this plugin will give a
false positive since it will think version `23.0` is the latest stable version of guava.
Some dependencies lacks proper meta data, like `commons-codec:commons-codec:1.15` then those dependencies will be listed
separately in the report. A warning is also printed on the console when executing the report task.

The license check task; `licenseCheck` checks all dependencies licenses if they are allowed or not. If the license name
is in any of the four pre-defined [files](./src/main/resources/se/solrike/otsswinfo/impl/) then it is considered to be allowed.

### Configure OTS SW info Plugin

Configure the `otsSwInfo` extension to use the tasks `versionReport` and `versionUpToDateReport`.

```groovy
otsSwInfo {
  excludeArtifactGroups = ['com.example.mylib']
  excludeOwnGroup = true // default true id the project has the group property set
  excludeProjects = ['my-cool-component-api','integration-testframework']
  // extra text to add to the report
  extraVersionInfo = ["Version description for $project.name $project.version"]
  // if an old report is submitted a "new to release" column is added to the version and license report.
  previousReportFile = layout.projectDirectory.file('config/versionReport/MyProduct_1_0_JavaVersionAndLicenseReport.csv')
  reportsDir = 'someFolder' // default build/reports/otsswinfo
  scanRootProject = false // default false if the project is a multiproject
  // in case the dependency lacks metadata you can add it here
  additionalLicenseMetadata = ['org.eclipse:swt:4.12.0' : 'EPL-1.0', 'com.richclientgui:rcptoolbox:1.0.10' : 'EPL-1.0']
  additionalUrlMetadata = ['com.ericsson.otp.erlang:otperlang:1.6.1' : 'https://www.erlang.org']
  additionalDescriptionMetadata = [
    'com.ericsson.otp.erlang:otperlang:1.6.1' : 'Erlang/OTP Java bridge',
    'com.sun.activation:jakarta.activation:1.2.2': 'Java service activation framework'
  }
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
  // optionally override the file with permissiveLicenses
  // the task simply checks the license text if it is present in the file.
  permissiveLicenses = layout.projectDirectory.file('permissiveLicenses.txt')
  // explicitly allow licenses in addition to the four licenses files
  allowedLicenses = ['Eclipse Distribution License - v 1.0']
  // explicit disallow some licenses that are default allowed by the plugin
  disallowedLicenses = ['GNU General Public License', 'GPL-1.0', 'GPL-1.0+', 'GPL-2.0', 'GPL-2.0+', 'GPL-3.0', 'GPL-3.0+']
  ignoreFailures = true // don't fail the build if disallowed license are used, default false
}
```

### Configure the version report (SBOM) to be compliant with OWASP SCVS
OWASP defines a standard for verification: [Software Component Verification Standard (SCVS)](https://owasp.org/www-project-software-component-verification-standard/).

Below is an example to configure the `versionReport` task to generate a SBOM in compliance with L1 of SCVS:

```groovy
otsSwInfo {
  extraVersionInfo = [
    "SBOM for $project.name $project.version",
    "ID: $project.group:$project.name:$project.version",
    "Timestamp: ${new Date()}"
  ]
}
```

### Configure the versionUpToDateReport task with open source policies for version age
OWASP [Software Component Verification Standard (SCVS)](https://owasp.org/www-project-software-component-verification-standard/) suggest that organizations have an open source policy to for instance check for
how many major or minor revisions old are acceptable.

The `versionUpToDateReport` can be configured with such policy:

```groovy
otsSwInfo {
  // don't allow any older major version compared to latest release/stable version.
  allowedOldMajorVersion = 0
  // allow upto 2 older minjor versions compared to latest release/stable version
  // in case major is the same.
  allowedOldMinorVersion = 2

}
```



## Sample reports
### Version and license report in CSV

|Name|Version|Package Name|Manufacturer URL|Description|License|
|-----|-----|-----|-----|-----|-----|
|slf4j-api|1.7.30|org.slf4j|http://www.slf4j.org|The slf4j API|MIT License|
|spring-aop|5.3.5|org.springframework|https://github.com/spring-projects/spring-framework|Spring AOP|Apache License, Version 2.0|


### Version up-to-date report in CSV

|Name|Version|Package Name|Latest|To old|
|-----|-----|-----|-----|-----|
|slf4j-api|1.7.30|org.slf4j|No - 1.7.36|No|
|spring-aop|5.3.5|org.springframework|No - 5.3.16|No|
|ion-java|1.0.2|software.amazon.ion|No - 1.5.1|Yes|
|httpclient|4.5.13|org.apache.httpcomponents|Yes|No|



## Allowed licenses
The list of allowed licenses are in four files:

* [Permissive](./src/main/resources/se/solrike/otsswinfo/impl/permissiveLicenses.txt)
* [Weak copyleft](./src/main/resources/se/solrike/otsswinfo/impl/weakCopyLeftLicenses.txt)
* [Strong copyleft](./src/main/resources/se/solrike/otsswinfo/impl/strongCopyLeftLicenses.txt)
* [GNU](./src/main/resources/se/solrike/otsswinfo/impl/gnuLicenses.txt)

They are in general fine to use if the software is hosted and not distributed.

## Release notes
### 1.0.0-beta.10
* Added configuration property to specify additional Gradle configuration scopes to search for dependencies. Default configuration `runtimeClasspath` from a Java project is searched.
* Added two properties where additional dependency metadata can be specified.
* Added  _Common Public License_  to weak copyleft licenses file.

### 1.0.0-beta.9
* More robust against missing info in POM files for dependencies.
* Added configuration property to specify additional metadata for dependencies in case the POM is missing info.

### 1.0.0-beta.8
* Possible to specify a policy for how old in terms of version a dependency is allowed to be.

### 1.0.0-beta.7
* Added configuration property to specify additional allowed licenses in addition to the four files.

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

