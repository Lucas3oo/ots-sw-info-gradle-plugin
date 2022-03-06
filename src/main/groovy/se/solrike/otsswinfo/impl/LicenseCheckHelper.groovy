package se.solrike.otsswinfo.impl

import org.gradle.api.file.RegularFileProperty

/**
 * @author Lucas Persson
 */
class LicenseCheckHelper {


  public String loadLicenses(RegularFileProperty licensesFile, String defaultFile) {
    if (licensesFile.present) {
      return licensesFile.getAsFile().get().text
    }
    else {
      return this.getClass().getResource(defaultFile).text
    }
  }
}
