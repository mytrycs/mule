/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.zernova.test.allure.AllureConstants.ScopeFeature.AsyncStory.ASYNC;

import com.zernova.functional.api.component.TestConnectorQueueHandler;
import com.zernova.runtime.api.artifact.Registry;

import jakarta.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Story(ASYNC)
public class ModuleAsyncTestCase extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Inject
  private Registry registry;

  private TestConnectorQueueHandler queueHandler;

  @Override
  protected String getModulePath() {
    return "modules/module-async.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-async.xml";
  }

  @Before
  public void before() throws Exception {
    queueHandler = new TestConnectorQueueHandler(registry);
  }

  @Test
  @Issue("MULE-19091")
  public void asyncWithNonBlockingOperation() throws Exception {
    flowRunner("asyncWithNonBlockingOperation").run();
    assertThat(queueHandler.read("asyncResponseQueue", RECEIVE_TIMEOUT), notNullValue());

    flowRunner("asyncWithNonBlockingOperation").run();
    assertThat(queueHandler.read("asyncResponseQueue", RECEIVE_TIMEOUT), notNullValue());
  }

}
