/*
 * Copyright © 2022 Cepheid. All Rights Reserved.
 */
package se.solrike.otsswinfo

/**
 * @author Lucas Persson
 */
public class IsStableDefault {

  /**
   * Default closure to determine if a version is latest or not.
   * <p>
   * Closure that takes a versions string and returns true if the version is considered stable.
   */
  public static Closure<Boolean> isStable = { String version ->
    return isStable(version)
  }

  /**
   * Check if the version is considered stable or not.
   *
   * @param version
   * @return true if either stable keyword or version is on "semantic version"-ish format.
   */
  public static boolean isStable(String version) {
    return isStableKeyword(version) || isStableVersion(version)
  }

  /**
   *
   * @param version
   * @return true if the version contains a stable keyword. Any of 'RELEASE', 'FINAL', 'GA' (case insensitive).
   */
  public static boolean isStableKeyword(String version) {
    return ['RELEASE', 'FINAL', 'GA'].any { keyword ->
      version.toUpperCase().contains(keyword)
    }
  }

  /**
   *
   * @param version
   * @return true if the version is on "semantic version"-ish format.
   */
  public static boolean isStableVersion(String version) {
    String regex = /^[0-9,.v-]+(-r)?$/
    return (version ==~ regex)
  }
}
