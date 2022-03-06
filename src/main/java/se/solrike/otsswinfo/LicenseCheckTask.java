package se.solrike.otsswinfo;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import se.solrike.otsswinfo.impl.ArtifactMetadata;
import se.solrike.otsswinfo.impl.LicenseCheckHelper;

/**
 * The task will scan all projects runtime dependencies and check licence info.
 * <p>
 * Any license not listed in any of the four licenses files will optionally fail the build.
 *
 * @author Lucas Persson
 */
public abstract class LicenseCheckTask extends OtsSwInfoBaseTask {

  /**
   * File with GNU licenses that are allowed.
   * <p>
   * E.g. "LGPL-2.1"
   *
   * @return file with multiple license texts
   */
  @InputFile
  @Optional
  public abstract RegularFileProperty getGnuLicenses();

  /**
   * File with permissive licenses that are allowed.
   * <p>
   * E.g. "Apache 2.0"
   *
   * @return file with multiple license texts
   */
  @InputFile
  @Optional
  public abstract RegularFileProperty getPermissiveLicenses();

  /**
   * File with strong copy left licenses that are allowed.
   * <p>
   * E.g. "BSD Protection License"
   *
   * @return file with multiple license texts
   */
  @InputFile
  @Optional
  public abstract RegularFileProperty getStrongCopyLeftLicenses();

  /**
   * File with weak copy left licenses that are allowed.
   * <p>
   * E.g. "EPL 2.0"
   *
   * @return file with multiple license texts
   */
  @InputFile
  @Optional
  public abstract RegularFileProperty getWeakCopyLeftLicenses();

  /**
   * Explicit list with disallowed licenses which takes precedence over the files with allowed licenses.
   *
   * @return list of disallowed licenses
   */
  @Input
  @Optional
  public abstract ListProperty<String> getDisallowedLicenses();

  /**
   * Whether or not this task will ignore failures and continue running the build.
   * <p>
   * Default false.
   *
   * @return true if failures should be ignored
   */
  @Input
  @Optional
  public abstract Property<Boolean> getIgnoreFailures();

  protected List<String> mLicensesList = new ArrayList<>(4);

  @TaskAction
  void run() {

    initExcludeArtifactGroupsAll();

    scanDependencies();

    LicenseCheckHelper l = new LicenseCheckHelper();
    mLicensesList.add(l.loadLicenses(getGnuLicenses(), "gnuLicenses.txt"));
    mLicensesList.add(l.loadLicenses(getPermissiveLicenses(), "permissiveLicenses.txt"));
    mLicensesList.add(l.loadLicenses(getStrongCopyLeftLicenses(), "strongCopyLeftLicenses.txt"));
    mLicensesList.add(l.loadLicenses(getWeakCopyLeftLicenses(), "weakCopyLeftLicenses.txt"));

    setHasPermissiveLicenses();

    long noofDepsWithDisallowedLicense = mDependencies.values()
        .stream()
        .filter(metaData -> !metaData.hasAllowedLicense)
        .count();

    String resultMessage = String.format("Number of OTS SW with disallowed licenses: %d",
        noofDepsWithDisallowedLicense);
    getLogger().error(resultMessage);
    if (Boolean.FALSE.equals(getIgnoreFailures().getOrElse(Boolean.FALSE)) && noofDepsWithDisallowedLicense > 0) {
      // fail build
      throw new GradleException(resultMessage);
    }
  }

  protected void setHasPermissiveLicenses() {
    for (ArtifactMetadata artifactMetadata : mDependencies.values()) {

      boolean isLicenseApproved = false;
      for (String licenses : mLicensesList) {
        isLicenseApproved = isLicenseApproved || licenses.contains(artifactMetadata.license);
      }
      isLicenseApproved = isLicenseApproved && !getDisallowedLicenses().get().contains(artifactMetadata.license);
      artifactMetadata.hasAllowedLicense = isLicenseApproved;
      if (!isLicenseApproved) {
        getLogger().error("Dependency {} has a disallowed license of '{}' from URL: {}", artifactMetadata.artifactName,
            artifactMetadata.license, artifactMetadata.licenseUrl);
      }
    }

  }

}
