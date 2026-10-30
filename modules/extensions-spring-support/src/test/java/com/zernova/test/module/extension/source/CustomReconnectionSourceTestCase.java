/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static com.zernova.tck.probe.PollingProber.probe;
import static com.zernova.test.allure.AllureConstants.ReconnectionPolicyFeature.RECONNECTION_POLICIES;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;
import static com.zernova.test.heisenberg.extension.ReconnectableHeisenbergSource.failedReconnections;
import static com.zernova.test.heisenberg.extension.ReconnectableHeisenbergSource.succesfulReconnections;
import static com.zernova.test.heisenberg.extension.ReconnectableHeisenbergSdkSource.failedReconnectionsSdk;
import static com.zernova.test.heisenberg.extension.ReconnectableHeisenbergSdkSource.succesfulReconnectionsSdk;

import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Feature;
import io.qameta.allure.Features;

@Features({@Feature(SOURCES), @Feature(RECONNECTION_POLICIES)})
public class CustomReconnectionSourceTestCase extends AbstractExtensionFunctionalTestCase {

  public static final int TIMEOUT_MILLIS = 50000;
  public static final int POLL_DELAY_MILLIS = 100;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "source/custom-reconnection-source-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    reset();
  }

  @Override
  protected void doTearDown() throws Exception {
    super.doTearDown();
    reset();
  }

  @Test
  public void successfulCustomReconnection() throws Exception {
    startFlow("successfulCustomReconnection");
    probe(TIMEOUT_MILLIS, POLL_DELAY_MILLIS, () -> succesfulReconnections > 1);
    assertThat(failedReconnections, is(0));
  }

  @Test
  public void failingCustomReconnection() throws Exception {
    startFlow("failingCustomReconnection");
    probe(TIMEOUT_MILLIS, POLL_DELAY_MILLIS, () -> failedReconnections > 0);
    assertThat(succesfulReconnections, is(0));
  }

  @Test
  public void successfulCustomReconnectionSdkApi() throws Exception {
    startFlow("successfulCustomReconnectionSdkApi");
    probe(TIMEOUT_MILLIS, POLL_DELAY_MILLIS, () -> succesfulReconnectionsSdk > 1);
    assertThat(failedReconnectionsSdk, is(0));
  }

  @Test
  public void failingCustomReconnectionSdkApi() throws Exception {
    startFlow("failingCustomReconnectionSdkApi");
    probe(TIMEOUT_MILLIS, POLL_DELAY_MILLIS, () -> failedReconnectionsSdk > 0);
    assertThat(succesfulReconnectionsSdk, is(0));
  }

  private void startFlow(String flowName) throws Exception {
    ((Flow) getFlowConstruct(flowName)).start();
  }

  private void reset() {
    succesfulReconnections = 0;
    failedReconnections = 0;
    failedReconnectionsSdk = 0;
    succesfulReconnectionsSdk = 0;
  }
}
