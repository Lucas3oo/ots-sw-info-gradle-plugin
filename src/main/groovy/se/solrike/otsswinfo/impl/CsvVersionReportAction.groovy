package se.solrike.otsswinfo.impl

/**
 * Generate a CSV report with tab as separator
 * 
 * @author Lucas Persson
 */
public class CsvVersionReportAction {

  final static String SEPARATOR = '\t'


  public void generateReport(File reportsDir, List<String> extraVersionInfo, boolean includeNewToRelease, List<ArtifactMetadata> dependencies) {
    reportsDir.mkdirs()
    File file = new File(reportsDir, "JavaVersionAndLicenseReport.csv")

    file.write "sep=${SEPARATOR}\n"
    extraVersionInfo.forEach({ extraInfo ->
      file << "$extraInfo\n"
    })
    file << '\n'
    file << "Name${SEPARATOR}"
    file << "Version${SEPARATOR}"
    file << "Package Name${SEPARATOR}"
    file << "Manufacturer URL${SEPARATOR}"
    file << "Description${SEPARATOR}"
    file << "License${SEPARATOR}"
    if (includeNewToRelease) {
      file << 'New to Release'
    }
    file << '\n'

    dependencies.forEach({metadata ->
      file << "$metadata.artifact.moduleName${SEPARATOR}"
      file << "$metadata.artifact.moduleVersion${SEPARATOR}"
      file << "$metadata.artifact.moduleGroup${SEPARATOR}"
      file << "$metadata.url${SEPARATOR}"
      file << "$metadata.description${SEPARATOR}"
      file << "$metadata.license${SEPARATOR}"
      if (includeNewToRelease) {
        file <<  (metadata.newToRelease ? 'Yes' : 'No')
      }
      file << '\n'
    })
  }
}
