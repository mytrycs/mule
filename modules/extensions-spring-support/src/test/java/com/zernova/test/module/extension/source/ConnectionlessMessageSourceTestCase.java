/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationState;
import com.zernova.runtime.extension.api.runtime.config.ConfiguredComponent;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

import io.qameta.allure.Feature;

@Feature(SOURCES)
public class ConnectionlessMessageSourceTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "source/connectionless-message-source.xml";
  }

  @Test
  public void obtainDisconnectedSourceConfigParameters() throws Exception {
    Component element = locator.find(Location.builder().globalName("source").addSourcePart().build()).get();
    assertThat(element, is(instanceOf(ConfiguredComponent.class)));

    final ConfigurationInstance configurationInstance = ((ConfiguredComponent) element).getConfigurationInstance().get();
    ConfigurationState configurationState = configurationInstance.getState();

    assertThat(configurationState.getConfigParameters().size(), is(0));
    assertThat(configurationState.getConnectionParameters().size(), is(0));
  }



}
