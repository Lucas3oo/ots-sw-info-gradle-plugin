package se.solrike.otsswinfo;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

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
   * In a multiproject it might not be interesting to scan the root project. Default false if the project is a
   * multiproject.
   *
   * @return true if only the root project shall be scanned.
   */
  Property<Boolean> getScanRootProject();

}
