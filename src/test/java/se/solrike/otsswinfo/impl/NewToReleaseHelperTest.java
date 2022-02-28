package se.solrike.otsswinfo.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.gradle.api.artifacts.ResolvedDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewToReleaseHelperTest {

  private ArtifactMetadata mArtifactMetadataNetty4173;
  private ArtifactMetadata mArtifactMetadataNetty4177;
  private String mPreviousReport;

  @BeforeEach
  final void setup() {
    mPreviousReport = "netty-buffer\t4.1.73.Final\tio.netty\thttp://example.com\nclassgraph\t4.8.102\tio.github.classgraph\thttps://example.org";

    ResolvedDependency dep = mock(ResolvedDependency.class);
    when(dep.getModuleName()).thenReturn("netty-buffer");
    when(dep.getModuleGroup()).thenReturn("io.netty");
    when(dep.getModuleVersion()).thenReturn("4.1.73.Final");
    mArtifactMetadataNetty4173 = new ArtifactMetadata("io.netty:netty-buffer:4.1.73.Final", dep);

    ResolvedDependency dep2 = mock(ResolvedDependency.class);
    when(dep2.getModuleName()).thenReturn("netty-buffer");
    when(dep2.getModuleGroup()).thenReturn("io.netty");
    when(dep2.getModuleVersion()).thenReturn("4.1.77.Final");
    mArtifactMetadataNetty4177 = new ArtifactMetadata("io.netty:netty-buffer:4.1.77.Final", dep2);

  }

  @Test
  final void testIsDependecyNewToReleaseFalse() {
    boolean newToRelease = NewToReleaseHelper.isDependecyNewToRelease(mArtifactMetadataNetty4173, mPreviousReport);
    assertThat(newToRelease).isFalse();
  }

  @Test
  final void testIsDependecyNewToReleaseTrue() {
    boolean newToRelease = NewToReleaseHelper.isDependecyNewToRelease(mArtifactMetadataNetty4177, mPreviousReport);
    assertThat(newToRelease).isTrue();
  }

}
