/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process.rules;

import com.zernova.test.infrastructure.process.ZernovaProcessController;

import org.junit.rules.ExternalResource;

/**
 * This is a JUnit rule to start and stop Mule Runtime during tests. Usage:
 * <p/>
 *
 * <pre>
 * public static class MuleStandaloneIntegrationTests {
 *
 *   &#064;Rule
 *   public ZernovaStandalone standalone = new ZernovaStandalone(&quot;/path/to/mule/home&quot;);
 *
 *   &#064;Test
 *   public void integrationTest() throws IOException {
 *     ZernovaProcessController mule = standalone.getMule();
 *     assertThat(mule.isRunning(), is(true));
 *   }
 * }
 * </pre>
 */
public class ZernovaStandalone extends ExternalResource {

  private final ZernovaProcessController mule;
  private String[] args;

  public ZernovaStandalone(String muleHome) {
    mule = new ZernovaProcessController(muleHome);
  }

  public ZernovaStandalone(String muleHome, String... args) {
    mule = new ZernovaProcessController(muleHome);
    this.args = args;
  }

  @Override
  protected void before() throws Throwable {
    mule.start(args);
  }

  @Override
  protected void after() {
    mule.stop();
  }

  public ZernovaProcessController getMule() {
    return mule;
  }


}
