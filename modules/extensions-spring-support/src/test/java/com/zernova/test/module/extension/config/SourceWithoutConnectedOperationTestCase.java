/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.config;

import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.vegan.extension.HarvestPeachesSource;

import org.junit.Test;

public class SourceWithoutConnectedOperationTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "operations/vegan-config.xml";
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    HarvestPeachesSource.isConnected = false;
    super.doSetUpBeforeMuleContextCreation();
  }

  @Test
  public void testSourceIsConnected() throws Exception {
    new PollingProber(1000, 1000).check(new JUnitLambdaProbe(() -> HarvestPeachesSource.isConnected));
  }

}
