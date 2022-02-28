package se.solrike.otsswinfo.impl

class NewToReleaseHelper {

  private String mPreviousReport;

  public NewToReleaseHelper(File previousReportFile) {
    mPreviousReport = previousReportFile.text;
  }

  boolean isDependecyNewToRelease(ArtifactMetadata metadata) {
    return isDependecyNewToRelease(metadata, mPreviousReport)
  }

  static boolean isDependecyNewToRelease(ArtifactMetadata metadata, String previousReport) {
    String regex = '.*' +  metadata.artifact.moduleName + '.*' + metadata.artifact.moduleVersion + '.*' + metadata.artifact.moduleGroup + '.*'
    return !(previousReport =~ regex)
  }
}
