package se.solrike.otsswinfo.impl

/**
 * Generate a CSV report with tab as separator
 *
 * @author Lucas Persson
 */
public class CsvVersionUpToDateReportAction {

  final static String SEPARATOR = '\t'


  public File generateReport(File reportsDir, List<String> extraVersionInfo, List<ArtifactMetadata> dependencies) {
    reportsDir.mkdirs()
    File file = new File(reportsDir, "JavaVersionUpToDateReport.csv")

    file.write "sep=${SEPARATOR}\n"
    extraVersionInfo.forEach({ extraInfo ->
      file << "$extraInfo\n"
    })
    file << '\n'
    file << "Name${SEPARATOR}"
    file << "Version${SEPARATOR}"
    file << "Package Name${SEPARATOR}"
    file << 'Latest'
    file << '\n'

    dependencies.forEach({metadata ->
      file << "$metadata.artifact.moduleName${SEPARATOR}"
      file << "$metadata.artifact.moduleVersion${SEPARATOR}"
      file << "$metadata.artifact.moduleGroup${SEPARATOR}"
      file << (metadata.latest ? 'Yes' : "No - $metadata.latestVersion")
      file << '\n'
    })
    return file
  }
}
