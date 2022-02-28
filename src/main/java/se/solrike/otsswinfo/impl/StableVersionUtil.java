package se.solrike.otsswinfo.impl;

import java.util.List;

/**
 * @author Lucas Persson
 */
public class StableVersionUtil {

  private StableVersionUtil() {
    // util class
  }

  public static boolean isStable(String version) {
    return stableKeyword(version) || stableVersion(version);
  }

  public static boolean stableKeyword(String version) {
    return List.of("RELEASE", "FINAL", "GA").stream().anyMatch(keyword -> version.toUpperCase().contains(keyword));
  }

  public static boolean stableVersion(String version) {
    String regex = "^[0-9,.v-]+(-r)?$";
    return version.matches(regex);
  }

}
