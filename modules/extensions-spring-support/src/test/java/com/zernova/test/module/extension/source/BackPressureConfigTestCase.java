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
import static com.zernova.runtime.api.component.location.Location.builderFromStringRepresentation;
import static com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy.DROP;
import static com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy.FAIL;
import static com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy.WAIT;
import static com.zernova.test.allure.AllureConstants.ExecutionEngineFeature.ExecutionEngineStory.BACKPRESSURE;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;

import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SOURCES)
@Story(BACKPRESSURE)
public class BackPressureConfigTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "source/heisenberg-backpressure-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    componentLocator = registry.lookupByType(ConfigurationComponentLocator.class).get();
  }

  @Test
  public void defaultsToFail() {
    assertStrategy("defaultToFail", FAIL);
  }

  @Test
  public void configuredToDrop() {
    assertStrategy("configuredToDrop", DROP);
  }

  @Test
  public void inheritDefaultFromParentSourceClass() {
    assertStrategy("defaultCase", FAIL);
  }

  private void assertStrategy(String flowName, BackPressureStrategy expected) {
    MessageSource source =
        (MessageSource) componentLocator.find(builderFromStringRepresentation(flowName + "/source").build()).get();
    assertThat(source.getBackPressureStrategy(), is(expected));
  }
}
