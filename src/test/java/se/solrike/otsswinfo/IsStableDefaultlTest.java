package se.solrike.otsswinfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Lucas Persson
 */
class IsStableDefaultlTest {

  @Test
  final void testStableVersion() {
    assertThat(IsStableDefault.isStable.call("1.0.0")).isTrue();
    assertThat(IsStableDefault.isStable.call("v1.0.0")).isTrue();
    assertThat(IsStableDefault.isStable.call("v1.0.0-alpha")).as("alpha in version indicate unstable").isFalse();
    assertThat(IsStableDefault.isStable.call("1.0.1")).isTrue();

    assertThat(IsStableDefault.isStable.call("1.0.1-final")).isTrue();
    assertThat(IsStableDefault.isStable.call("1.0.1.Final")).isTrue();
    assertThat(IsStableDefault.isStable.call("v2.0-GA")).isTrue();
    assertThat(IsStableDefault.isStable.call("1.0.alpha")).as("alpha in version indicate unstable").isFalse();
    assertThat(IsStableDefault.isStable.call("1.0.1c")).as("not semantic version").isFalse();
    assertThat(IsStableDefault.isStable.call("30.3-jre")).as("having build suffix not indicate a stable release")
        .isFalse();
    assertThat(IsStableDefault.isStable.call("30.3+jre")).as("having build suffix not indicate a stable release")
        .isFalse();
    assertThat(IsStableDefault.isStable.call("1.0.release")).isTrue();
  }

}
