package se.solrike.otsswinfo.impl

/**
 * Generate a CSV report with tab as separator
 *
 * @author Lucas Persson
 */
public class CsvVersionUpToDateReportAction {


  public File generateReport(String separator, File reportsDir, List<String> extraVersionInfo, List<ArtifactMetadata> dependencies) {
    // calculate max padding
    int namePadding = 0
    int versionPadding = 0
    int groupPadding = 0
    dependencies.forEach({metadata ->
      namePadding = Math.max(metadata.artifact.moduleName.size(), namePadding)
      versionPadding = Math.max(metadata.artifact.moduleVersion.size(), versionPadding)
      groupPadding = Math.max(metadata.artifact.moduleGroup.size(), groupPadding)
    })

    // generate the report
    reportsDir.mkdirs()
    File file = new File(reportsDir, "JavaVersionUpToDateReport.csv")

    file.write "sep=${separator}\n"
    extraVersionInfo.forEach({ extraInfo ->
      file << "$extraInfo\n"
    })
    file << '\n'
    file << "Name".padRight(namePadding) + separator
    file << "Version".padRight(versionPadding) + separator
    file << "Package Name".padRight(groupPadding) + separator
    file << 'Latest'
    file << '\n'

    dependencies.forEach({metadata ->
      file << "${metadata.artifact.moduleName.padRight(namePadding)}${separator}"
      file << "${metadata.artifact.moduleVersion.padRight(versionPadding)}${separator}"
      file << "${metadata.artifact.moduleGroup.padRight(groupPadding)}${separator}"
      file << (metadata.latest ? 'Yes' : "No - $metadata.latestVersion")
      file << '\n'
    })
    return file
  }
}
