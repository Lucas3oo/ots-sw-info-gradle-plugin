package se.solrike.otsswinfo;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import groovy.lang.Closure;

/**
 * @author Lucas Persson
 */
public interface OtsSwInfoExtension {

  /**
   * Dependencies to exclude from the report. Identified via groupId. e.g. com.example.mylib
   *
   * @return list of dependencies group name to exclude from the report
   */
  ListProperty<String> getExcludeArtifactGroups();

  /**
   * Exclude dependencies with same groupId as the project that applies this plugin.
   * <p>
   * Default true.
   *
   * @return true is exclude with same groupId as the project
   */
  Property<Boolean> getExcludeOwnGroup();

  /**
   *
   * @return list of sub project names to exclude from the scan
   */
  ListProperty<String> getExcludeProjects();

  /**
   * Additional text to add in the beginning of the report
   *
   * @return list of string to add to the report
   */
  ListProperty<String> getExtraVersionInfo();

  /**
   * Previous report to compare with in order to set the "new to release" flag.
   *
   * @return the file with the previous release of this project's dependencies.
   */
  RegularFileProperty getPreviousReportFile();

  /**
   * The default directory where reports will be generated.
   *
   * @return reports main directory
   */
  DirectoryProperty getReportsDir();

  /**
   * CSV separator. Default ",".
   *
   * @return CSV separator character
   */
  Property<String> getReportCsvSeparator();

  /**
   * In a multiproject it might not be interesting to scan the root project. Default false if the project is a
   * multiproject.
   *
   * @return true if only the root project shall be scanned.
   */
  Property<Boolean> getScanRootProject();

  /**
   * Closure that takes a version string and return true if it is considered a stable version.
   * <p>
   * Default is {@link IsStableDefault#isStable}
   *
   * @return closure for evaluating a version
   */
  Property<Closure<Boolean>> getIsStable();

  /**
   * File with GNU licenses that are allowed.
   * <p>
   * E.g. "LGPL-2.1"
   *
   * @return file with multiple license texts
   */
  RegularFileProperty getGnuLicenses();

  /**
   * File with permissive licenses that are allowed.
   * <p>
   * E.g. "Apache 2.0"
   *
   * @return file with multiple license texts
   */
  RegularFileProperty getPermissiveLicenses();

  /**
   * File with strong copy left licenses that are allowed.
   * <p>
   * E.g. "BSD Protection License"
   *
   * @return file with multiple license texts
   */
  RegularFileProperty getStrongCopyLeftLicenses();

  /**
   * File with weak copy left licenses that are allowed.
   * <p>
   * E.g. "EPL 2.0"
   *
   * @return file with multiple license texts
   */
  RegularFileProperty getWeakCopyLeftLicenses();

  /**
   * Explicit list with allowed licenses in addition to the four licenses files.
   *
   * @return list of allowed licenses
   */
  ListProperty<String> getAllowedLicenses();

  /**
   * Explicit list with disallowed licenses which takes precedence over the files with allowed licenses.
   *
   * @return list of disallowed licenses
   */
  ListProperty<String> getDisallowedLicenses();

  /**
   * Whether or not this task will ignore failures and continue running the build.
   * <p>
   * Default false.
   *
   * @return true if failures should be ignored
   */
  Property<Boolean> getIgnoreFailures();

}
