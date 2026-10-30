/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static com.zernova.tck.probe.PollingProber.check;
import static com.zernova.tck.probe.PollingProber.checkNot;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SourcesStories.POLLING;

import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.petstore.extension.PooledPetStoreConnectionProvider;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SOURCES)
@Story(POLLING)
public class PollingSourceReleasesConnectionTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "source/polling-source-releases-connection-config.xml";
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    PooledPetStoreConnectionProvider.TIMES_CONNECTED = 0;
  }

  @Test
  public void watermarkedItemsReleaseConnectionsTestCase() throws Exception {
    startFlow("watermarkWithPooledConnection");
    check(10000, 100, () -> PooledPetStoreConnectionProvider.TIMES_CONNECTED > 0);
    checkNot(10000, 1000, () -> PooledPetStoreConnectionProvider.TIMES_CONNECTED > 10);
  }

  private void startFlow(String flowName) throws Exception {
    ((Startable) getFlowConstruct(flowName)).start();
  }

}
