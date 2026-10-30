/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.test;

import static com.zernova.test.allure.AllureConstants.ComponentsFeature.CORE_COMPONENTS;
import static com.zernova.test.allure.AllureConstants.ComponentsFeature.AddVariableStory.ADD_VARIABLE;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import com.zernova.functional.transformer.simple.AbstractAddVariablePropertyProcessorTestCase;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.processor.simple.AddFlowVariableProcessor;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(CORE_COMPONENTS)
@Story(ADD_VARIABLE)
@SmallTest
public class AddFlowVariableProcessorTestCase extends AbstractAddVariablePropertyProcessorTestCase {

  public AddFlowVariableProcessorTestCase() {
    super(new AddFlowVariableProcessor());
  }

  @Override
  protected void verifyAdded(CoreEvent event, String key, String value) {
    assertThat(event.getVariables().get(key).getValue(), is(value));
  }

  @Override
  protected void verifyNotAdded(CoreEvent event) {
    assertThat(event.getVariables().keySet(), empty());
  }

  @Override
  protected void verifyRemoved(CoreEvent event, String key) {
    assertThat(event.getVariables().keySet(), not(contains(key)));
  }

  @Override
  protected DataType getVariableDataType(CoreEvent event, String key) {
    return event.getVariables().get(key).getDataType();
  }

}
