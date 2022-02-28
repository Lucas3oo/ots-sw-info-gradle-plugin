package se.solrike.otsswinfo.impl

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolveException

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult

/**
 * @author Lucas Persson
 */
class ArtifactMetadataUtil {

  /**
   * @param artifactName on format group:moduleName:version
   */
  public static ArtifactMetadata updateArtifactMetadataRecursive(Project project, String artifactName, ArtifactMetadata artifactMetadata) {
    Dependency d = project.dependencies.create("$artifactName@pom")
    Configuration pomConfiguration = project.configurations.detachedConfiguration(d)

    File pomFile
    try {
      pomFile = pomConfiguration.resolve().asList().first()
    } catch (ResolveException e) {
      project.logger.warn("Unable to retrieve license for $artifactName since the POM file could not be resolved." + e)
      return artifactMetadata
    }


    XmlSlurper slurper = new XmlSlurper(true, false)
    slurper.setErrorHandler(new org.xml.sax.helpers.DefaultHandler())

    GPathResult pomXml
    try {
      pomXml = slurper.parse(pomFile)
    } catch (org.xml.sax.SAXParseException e) {
      project.logger.warn("Unable to parse POM file for $artifactName")
      return artifactMetadata
    }

    // only set description if it is not set so the actual artifact description is used and not the parent
    if (!artifactMetadata.description && pomXml.description.text()) {
      // remove newlines and multiple spaces
      String desc = pomXml.description.text().trim().replaceAll("\\r?\\n|\\r", " ")
      desc = desc.replaceAll("\\s+", " ")
      if (!desc.equals('${project.name}')) {
        artifactMetadata.description = desc
      }
    }

    // only set if not set already
    if (artifactMetadata.license == null && pomXml.licenses.license.size() > 0) {
      artifactMetadata.license = pomXml.licenses.license.first().name.text().trim()
      artifactMetadata.licenseUrl = pomXml.licenses.license.first().url.text().trim()
    }

    // only set if not set already
    if (artifactMetadata.url == null && pomXml.url.text()) {
      artifactMetadata.url = pomXml.url.text().trim()
    }

    if ((artifactMetadata.license == null || artifactMetadata.url == null) && pomXml.parent.text()) {
      // follow the parent to see if there is any license info or URL there
      // but keep the description from the initial pom
      String parentGroup = pomXml.parent.groupId.text().trim()
      String parentName = pomXml.parent.artifactId.text().trim()
      String parentVersion = pomXml.parent.version.text().trim()

      artifactMetadata = updateArtifactMetadataRecursive(project, "$parentGroup:$parentName:$parentVersion", artifactMetadata)
    }
    return artifactMetadata
  }
}
