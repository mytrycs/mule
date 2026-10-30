/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.connector;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static com.zernova.runtime.core.internal.retry.DefaultReconnectionConfig.DISABLE_ASYNC_RETRY_POLICY_ON_SOURCES;
import static com.zernova.tck.probe.PollingProber.probe;
import static com.zernova.test.petstore.extension.FailingPetStoreSource.connectionException;
import static com.zernova.test.petstore.extension.FailingPetStoreSource.executor;

import com.zernova.runtime.core.api.retry.policy.RetryPolicyExhaustedException;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.petstore.extension.FailingPetStoreSource;
import com.zernova.test.petstore.extension.PetStoreConnector;
import com.zernova.test.runner.RunnerDelegateTo;

import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class PetStoreSourceRetryPolicyFailsDeploymentTestCase extends AbstractExtensionFunctionalTestCase {

  public static final int TIMEOUT_MILLIS = 20000;
  public static final int POLL_DELAY_MILLIS = 50;

  @Rule
  public ExpectedException exception = none();

  @Parameterized.Parameter(0)
  public boolean failsDeployment;

  @Parameterized.Parameter(1)
  public String connectionConfig;

  @Parameterized.Parameter(2)
  public String sourceConfig;

  @Parameterized.Parameter(3)
  public int expectedRetries;


  @Parameterized.Parameters(name = "{1} - {2} - failsDeployment: {0}")
  public static Collection<Object[]> data() {
    return asList(
                  new Object[] {false, "petstore-connection-dont-fail-deployment.xml",
                      "petstore-source-retry-policy-error.xml", 2},
                  new Object[] {true, "petstore-connection-fail-deployment.xml",
                      "petstore-source-retry-policy-error.xml", 2},
                  new Object[] {false, "petstore-connection-dont-fail-deployment.xml",
                      "petstore-source-retry-policy-connection-exception.xml", 3},
                  new Object[] {false, "petstore-connection-fail-deployment.xml",
                      "petstore-source-retry-policy-connection-exception.xml", 3});
  }

  @Rule
  public SystemProperty muleDisableAsyncRetryPolicyOnSourcesProperty =
      new SystemProperty(DISABLE_ASYNC_RETRY_POLICY_ON_SOURCES, "false");

  @Override
  protected String[] getConfigFiles() {
    return new String[] {connectionConfig, sourceConfig};
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    PetStoreConnector.clearTimesStarted();
    FailingPetStoreSource.failedDueOnException = false;
    if (failsDeployment) {
      exception.expect(RetryPolicyExhaustedException.class);
      exception.expectCause(sameInstance(connectionException));
    } else {
      exception = none();
    }

    super.doSetUpBeforeMuleContextCreation();
  }

  @Override
  protected void doTearDown() throws Exception {
    PetStoreConnector.clearTimesStarted();
    FailingPetStoreSource.failedDueOnException = false;
    if (executor != null) {
      executor.shutdownNow();
    }

    super.doTearDown();
  }

  @Test
  public void retryPolicySourceFailOnStart() throws Exception {
    probe(TIMEOUT_MILLIS, POLL_DELAY_MILLIS, () -> {
      assertThat(PetStoreConnector.getTimesStarted(), is(expectedRetries));
      return true;
    });
  }

}
