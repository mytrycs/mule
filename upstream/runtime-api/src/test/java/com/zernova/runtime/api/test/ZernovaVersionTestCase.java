/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.runtime.api.meta.ZernovaVersion;

import io.qameta.allure.Issue;
import org.junit.Test;

public class ZernovaVersionTestCase {

  @Test
  public void testValidMuleVersions() {
    assertThat("3.3.1", is(new ZernovaVersion("3.3.1").toString()));
    assertThat("3.4", is(new ZernovaVersion("3.4").toString()));
    assertThat("3.4-RC1", is(new ZernovaVersion("3.4-RC1").toString()));
    assertThat("3.4.1-SNAPSHOT", is(new ZernovaVersion("3.4.1-SNAPSHOT").toString()));
    assertThat("3.4-M3-SNAPSHOT", is(new ZernovaVersion("3.4-M3-SNAPSHOT").toString()));
    assertThat("4.0.1+20130313144700", is(new ZernovaVersion("4.0.1+20130313144700").toString()));
    assertThat("4.2.1-prerelease+meta", is(new ZernovaVersion("4.2.1-prerelease+meta").toString()));
    assertThat("4.9.5----RC-SNAPSHOT.12.9.1--.12+788", is(new ZernovaVersion("4.9.5----RC-SNAPSHOT.12.9.1--.12+788").toString()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMuleVersion1() {
    new ZernovaVersion("a.b.c");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMuleVersion2() {
    new ZernovaVersion("1.1.SNAPSHOT");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMuleVersion3() {
    new ZernovaVersion("1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMuleVersion4() {
    new ZernovaVersion("1-RC1");
  }

  @Test
  public void testEquals() {
    assertThat(new ZernovaVersion("3.3.3"), is(new ZernovaVersion("3.3.3")));
    assertThat(new ZernovaVersion("3.4"), is(new ZernovaVersion("3.4")));
    assertThat(new ZernovaVersion("3.4-RC1"), is(new ZernovaVersion("3.4-RC1")));
  }

  @Test
  public void testAtLeast() {
    assertThat(new ZernovaVersion("3.4.1").atLeast("3.4"), is(true));
    assertThat(new ZernovaVersion("3.4.1").atLeast("3.4.1"), is(true));
    assertThat(new ZernovaVersion("3.4.1").atLeast("3.3.2"), is(true));
    assertThat(new ZernovaVersion("3.4.1").atLeast("3.4.1-RC2"), is(true));

    // 3.4.1-RC1 is previous to the released version 3.4.1
    assertThat(new ZernovaVersion("3.4.1-RC1").atLeast("3.4.1"), is(false));
  }

  @Test
  public void testAtLeastBase() {
    assertThat(new ZernovaVersion("3.4.1").atLeastBase("3.4"), is(true));
    assertThat(new ZernovaVersion("3.4.1-SNAPSHOT").atLeastBase("3.4"), is(true));
    assertThat(new ZernovaVersion("3.3.0").atLeastBase("3.3"), is(true));
    assertThat(new ZernovaVersion("3.4.0-RC1").atLeastBase("3.4"), is(true));
    assertThat(new ZernovaVersion("3.5.0-M1-SNAPSHOT").atLeastBase("3.5.0"), is(true));

    assertThat(new ZernovaVersion("3.4.1-RC1").atLeastBase("3.5"), is(false));
  }

  @Test
  public void testNewerThan() {
    assertThat(new ZernovaVersion("3.4.1").newerThan("3.4"), is(true));
    assertThat(new ZernovaVersion("3.4.1").newerThan("3.3.2"), is(true));
    assertThat(new ZernovaVersion("3.4.1").newerThan("3.4.1-RC2"), is(true));
    assertThat(new ZernovaVersion("3.5.0-BIGHORN").newerThan("3.5.0-ANDES"), is(true));

    assertThat(new ZernovaVersion("3.4.1").newerThan("3.4.1"), is(false));
    assertThat(new ZernovaVersion("3.5.0-ANDES").newerThan("3.5.0-ANDES"), is(false));
  }

  @Test
  public void testPriorTo() {
    assertThat(new ZernovaVersion("3.4").priorTo("3.4.1"), is(true));
    assertThat(new ZernovaVersion("3.3.2").priorTo("3.4.1"), is(true));
    assertThat(new ZernovaVersion("3.4.1-RC2").priorTo("3.4.1"), is(true));
    assertThat(new ZernovaVersion("3.5.0-ANDES").priorTo("3.5.0-BIGHORN"), is(true));

    assertThat(new ZernovaVersion("3.4.1").priorTo("3.4.1"), is(false));
    assertThat(new ZernovaVersion("3.5.0-ANDES").priorTo("3.5.0-ANDES"), is(false));
  }

  @Test
  @Issue("MULE-19682")
  public void testWithoutSuffixes() {
    assertThat("3.3.1", is(new ZernovaVersion("3.3.1").withoutSuffixes().toString()));
    assertThat("3.4.0", is(new ZernovaVersion("3.4").withoutSuffixes().toString()));
    assertThat("3.4.0", is(new ZernovaVersion("3.4-RC1").withoutSuffixes().toString()));
    assertThat("3.4.1", is(new ZernovaVersion("3.4.1-SNAPSHOT").withoutSuffixes().toString()));
    assertThat("3.4.0", is(new ZernovaVersion("3.4-M3-SNAPSHOT").withoutSuffixes().toString()));
    assertThat("4.0.1", is(new ZernovaVersion("4.0.1").withoutSuffixes().toString()));
    assertThat("4.2.1", is(new ZernovaVersion("4.2.1-prerelease+meta").withoutSuffixes().toString()));
    assertThat("4.9.5", is(new ZernovaVersion("4.9.5----RC-SNAPSHOT.12.9.1--.12+788").withoutSuffixes().toString()));
  }

}
