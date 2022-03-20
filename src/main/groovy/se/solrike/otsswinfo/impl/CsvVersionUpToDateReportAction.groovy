package se.solrike.otsswinfo.impl

/**
 * Generate a CSV report
 *
 * @author Lucas Persson
 */
public class CsvVersionUpToDateReportAction {


  public File generateReport(String separator, File reportsDir, List<String> extraVersionInfo,
      List<ArtifactMetadata> dependencies, Collection<ArtifactMetadata> nonDetermineDependencies) {
    // calculate max padding
    int namePadding = 0
    int versionPadding = 0
    int groupPadding = 0
    int latestPadding = 3 // size of Yes
    dependencies.forEach({ metadata ->
      namePadding = Math.max(metadata.artifact.moduleName.size(), namePadding)
      versionPadding = Math.max(metadata.artifact.moduleVersion.size(), versionPadding)
      groupPadding = Math.max(metadata.artifact.moduleGroup.size(), groupPadding)
      if (!metadata.latest) {
        int widht = (metadata.latestVersion ? metadata.latestVersion.size()+5: 0)
        latestPadding = Math.max(widht, latestPadding)
      }
    })

    // generate the report
    reportsDir.mkdirs()
    File file = new File(reportsDir, "JavaVersionUpToDateReport.csv")

    file.write "sep=${separator}\n"
    extraVersionInfo.forEach({ extraInfo ->
      file << "$extraInfo\n"
    })
    file << '\n'

    file << 'Following dependencies could not be analysed:\n'
    nonDetermineDependencies.forEach({metadata ->
      file << "${metadata.artifactName}"
      file << '\n'
    })
    file << '\n'

    file << "Name".padRight(namePadding) + separator
    file << "Version".padRight(versionPadding) + separator
    file << "Package Name".padRight(groupPadding) + separator
    file << "Latest".padRight(latestPadding) + separator
    file << 'To old'
    file << '\n'

    dependencies.forEach({ metadata ->
      file << "${metadata.artifact.moduleName.padRight(namePadding)}${separator}"
      file << "${metadata.artifact.moduleVersion.padRight(versionPadding)}${separator}"
      file << "${metadata.artifact.moduleGroup.padRight(groupPadding)}${separator}"
      String latest = (metadata.latest ? 'Yes' : "No - ${metadata.latestVersion}")
      file << "${latest.padRight(latestPadding)}${separator}"
      file << "${getIsTooOldVersion(metadata.isTooOldVersion)}"
      file << '\n'
    })
    return file
  }

  static String getIsTooOldVersion(Boolean isTooOldVersion) {
    if (isTooOldVersion != null) {
      return isTooOldVersion ? 'Yes' : 'No'
    }
    else {
      return ''
    }
  }
}
