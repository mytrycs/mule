/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.model;

import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.GLOBAL_ERROR_HANDLER;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
@Issue("W-11117613")
@Feature(ERROR_HANDLING)
@Story(GLOBAL_ERROR_HANDLER)
public class CoreComponentBuildingDefinitionProviderTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private CoreComponentBuildingDefinitionProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new CoreComponentBuildingDefinitionProvider();
  }

  @Test
  public void isPrototype() {
    ComponentBuildingDefinition buildingDefinition = provider.getErrorHandlerBuilder().build();

    assertThat(buildingDefinition.isPrototype(), is(false));
  }
}
