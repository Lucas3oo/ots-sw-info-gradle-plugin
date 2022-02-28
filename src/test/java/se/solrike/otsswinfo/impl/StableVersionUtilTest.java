package se.solrike.otsswinfo.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Lucas Persson
 */
class StableVersionUtilTest {

  @Test
  final void testStableVersion() {
    assertThat(StableVersionUtil.isStable("1.0.0")).isTrue();
    assertThat(StableVersionUtil.isStable("v1.0.0")).isTrue();
    assertThat(StableVersionUtil.isStable("v1.0.0-alpha")).as("alpha in version indicate unstable").isFalse();
    assertThat(StableVersionUtil.isStable("1.0.1")).isTrue();

    assertThat(StableVersionUtil.isStable("1.0.1-final")).isTrue();
    assertThat(StableVersionUtil.isStable("1.0.1.Final")).isTrue();
    assertThat(StableVersionUtil.isStable("v2.0-GA")).isTrue();
    assertThat(StableVersionUtil.isStable("1.0.alpha")).as("alpha in version indicate unstable").isFalse();
    assertThat(StableVersionUtil.isStable("1.0.1c")).as("not semantic version").isFalse();
    assertThat(StableVersionUtil.isStable("30.3-jre")).as("having build suffix not indicate a stable release")
        .isFalse();
    assertThat(StableVersionUtil.isStable("30.3+jre")).as("having build suffix not indicate a stable release")
        .isFalse();
    assertThat(StableVersionUtil.isStable("1.0.release")).isTrue();
  }

}
