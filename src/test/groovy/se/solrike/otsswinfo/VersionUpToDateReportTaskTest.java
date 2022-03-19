package se.solrike.otsswinfo;

import static org.assertj.core.api.Assertions.*;
import static se.solrike.otsswinfo.VersionUpToDateReportTask.*;

import org.junit.jupiter.api.Test;

class VersionUpToDateReportTaskTest {

  @Test
  void testIsTooOld() {
    assertThat(isTooOld(0, 3, "1.2.3", "2.0.2")).isTrue();
    assertThat(isTooOld(0, 3, "1.0.3", "1.3.2")).isFalse();
    assertThat(isTooOld(0, 3, "1.0.3", "1.4.2")).isTrue();
    assertThat(isTooOld(1, 3, "1.2.3", "2.5.2")).isFalse();
    assertThat(isTooOld(1, 3, "1.2.3-jre", "2.7.2-jre")).isFalse();
    assertThat(isTooOld(1, 2, "1.0.2.Final", "2.2.3.Final")).isFalse();
  }

}
