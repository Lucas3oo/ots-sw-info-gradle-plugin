package se.solrike.otsswinfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionUpToDateReportTaskTest {

  @Test
  final void testStableVersion() {
    assertThat(VersionUpToDateReportTask.stableVersion("1.0.0")).isTrue();
    assertThat(VersionUpToDateReportTask.stableVersion("v1.0.0")).isTrue();
    assertThat(VersionUpToDateReportTask.stableVersion("v1.0.0-alpha")).as("alpha in version indicate unstable")
        .isFalse();
    assertThat(VersionUpToDateReportTask.stableVersion("1.0.1")).isTrue();
  }

  @Test
  final void testStableKeyword() {
    assertThat(VersionUpToDateReportTask.stableKeyword("final")).isTrue();
    assertThat(VersionUpToDateReportTask.stableKeyword("GA")).isTrue();
    assertThat(VersionUpToDateReportTask.stableKeyword("alpha")).as("alpha in version indicate unstable").isFalse();
    assertThat(VersionUpToDateReportTask.stableKeyword("release")).isTrue();
  }

}
