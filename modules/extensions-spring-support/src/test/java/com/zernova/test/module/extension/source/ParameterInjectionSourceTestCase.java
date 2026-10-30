/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static com.zernova.tck.probe.PollingProber.probe;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;
import static com.zernova.test.module.extension.source.HeisenbergMessageSourceTestCase.POLL_DELAY_MILLIS;
import static com.zernova.test.module.extension.source.HeisenbergMessageSourceTestCase.TIMEOUT_MILLIS;

import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.test.heisenberg.extension.HeisenbergSource;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.qameta.allure.Feature;

@Feature(SOURCES)
public class ParameterInjectionSourceTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "source/map-parameter-injection-config.xml";
  }

  @Override
  public Map<String, Object> getStartUpRegistryObjects() {
    Map<String, Object> customProperties = new HashMap<>();
    Map<String, Object> debtConfiguration = new HashMap<>();

    debtConfiguration.put("maxDebt", "Jesse's");
    debtConfiguration.put("minDebt", "Krazy-8");

    customProperties.put("referableDebtProperties", debtConfiguration);

    return customProperties;
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    HeisenbergSource.receivedDebtProperties = null;
    HeisenbergSource.receivedUsableWeapons = null;
  }

  @Override
  protected void doTearDown() throws Exception {
    super.doTearDown();
    HeisenbergSource.receivedDebtProperties = null;
    HeisenbergSource.receivedUsableWeapons = null;
  }

  @Test
  public void injectMapParameters() throws Exception {
    Flow flow = (Flow) getFlowConstruct("source");
    flow.start();

    probe(TIMEOUT_MILLIS, POLL_DELAY_MILLIS,
          () -> HeisenbergSource.receivedDebtProperties != null && HeisenbergSource.receivedDebtProperties.containsKey("maxDebt")
              && HeisenbergSource.receivedUsableWeapons != null && HeisenbergSource.receivedUsableWeapons.containsKey("Ricin"));

  }
}
