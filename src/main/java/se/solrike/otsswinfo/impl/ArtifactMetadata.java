package se.solrike.otsswinfo.impl;

import java.util.Objects;

import org.gradle.api.artifacts.ResolvedDependency;

/**
 * @author Lucas Persson
 */
@SuppressWarnings("java:S1104")
public class ArtifactMetadata implements Comparable<ArtifactMetadata> {

  public ArtifactMetadata(String artifactName, ResolvedDependency artifact) {
    this.artifactName = artifactName;
    this.artifact = artifact;
  }

  /**
   * the full name as GAV (group:moduleName:version)
   */
  public String artifactName;
  public ResolvedDependency artifact;
  public String license;
  public String licenseUrl;
  /**
   * project's URL
   */
  public String url;
  public String description = "";
  public Boolean newToRelease;
  // if null then it wasn't possible to figure it out
  public String latestVersion;
  public Boolean hasAllowedLicense;
  // if null then it wasn't possible to figure it out
  public Boolean isTooOldVersion;

  /**
   *
   * @return true if the current version is already latest version
   */
  public boolean isLatest() {
    return artifact.getModuleVersion().equals(latestVersion);
  }

  @Override
  public int compareTo(ArtifactMetadata other) {
    return artifact.getModuleName().toLowerCase().compareTo(other.artifact.getModuleName().toLowerCase());
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifact.getModuleName().toLowerCase());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ArtifactMetadata other = (ArtifactMetadata) obj;
    return Objects.equals(artifact.getModuleName().toLowerCase(), other.artifact.getModuleName().toLowerCase());
  }

  @Override
  public String toString() {
    return "ArtifactMetadata [artifactName=" + artifactName + ", license=" + license + ", latestVersion="
        + latestVersion + "]";
  }

}
