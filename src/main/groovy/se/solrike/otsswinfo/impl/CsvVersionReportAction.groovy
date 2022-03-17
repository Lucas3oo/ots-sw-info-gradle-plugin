package se.solrike.otsswinfo.impl

/**
 * Generate a CSV report with tab as separator
 *
 * @author Lucas Persson
 */
public class CsvVersionReportAction {

  public File generateReport(String separator, File reportsDir, List<String> extraVersionInfo, boolean includeNewToRelease, List<ArtifactMetadata> dependencies) {
    reportsDir.mkdirs()
    File file = new File(reportsDir, "JavaVersionAndLicenseReport.csv")

    file.write "sep=${separator}\n"
    extraVersionInfo.forEach({ extraInfo ->
      file << "$extraInfo\n"
    })
    file << '\n'
    file << "Name${separator}"
    file << "Version${separator}"
    file << "Package Name${separator}"
    file << "Manufacturer URL${separator}"
    file << "Description${separator}"
    file << "License${separator}"
    if (includeNewToRelease) {
      file << 'New to Release'
    }
    file << '\n'

    dependencies.forEach({metadata ->
      file << "$metadata.artifact.moduleName${separator}"
      file << "$metadata.artifact.moduleVersion${separator}"
      file << "$metadata.artifact.moduleGroup${separator}"
      file << "\"${escape(metadata.url)}\"${separator}"
      file << "\"${escape(metadata.description)}\"${separator}"
      file << "\"${escape(metadata.license)}\"${separator}"
      if (includeNewToRelease) {
        file <<  (metadata.newToRelease ? 'Yes' : 'No')
      }
      file << '\n'
    })
    return file
  }

  // escape " with "" as specified in https://www.ietf.org/rfc/rfc4180.txt
  protected String escape(String value) {
    return (value != null ? value.replace('"', '""') : '')
  }
}
