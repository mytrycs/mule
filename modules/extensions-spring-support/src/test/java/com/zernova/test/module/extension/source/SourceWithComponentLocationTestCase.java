/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static com.zernova.test.allure.AllureConstants.ConfigurationComponentLocatorFeature.ConfigurationComponentLocationStory.COMPONENT_LOCATION;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.petstore.extension.SentientSource;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SOURCES)
@Story(COMPONENT_LOCATION)
public class SourceWithComponentLocationTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "source/source-with-component-location-config.xml";
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    super.doSetUpBeforeMuleContextCreation();
    reset();
  }

  @Override
  protected void doTearDown() throws Exception {
    super.doTearDown();
    reset();
  }

  @Test
  public void injectedComponentLocation() throws Exception {
    new PollingProber(5000, 50)
        .check(new JUnitLambdaProbe(() -> {
          ComponentLocation location = SentientSource.capturedLocation;
          assertThat(location, is(notNullValue()));
          assertThat(location.getRootContainerName(), equalTo("sentient"));
          return true;
        }));
  }

  private void reset() {
    SentientSource.capturedLocation = null;
  }
}
