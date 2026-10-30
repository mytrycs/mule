/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.core.api.event.CoreEvent;

import io.qameta.allure.Description;
import org.junit.Test;

public class ModuleStereotypesTestCase extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Override
  protected String getModulePath() {
    return "modules/module-stereotypes.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-stereotypes.xml";
  }

  @Test
  @Description("Runtime test to ensure parameterized stereotypes in global element works")
  public void parameterizedStereotypeDoesCorrectBindingAndConnectorWorks() throws Exception {
    final String data = "some parameterized data";
    final CoreEvent muleEvent = flowRunner("testParameterizedStereotypeDoesCorrectBindingAndConnectorWorks")
        .withVariable("aData", data).run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is(data));
  }

  @Test
  @Description("Runtime test to ensure parameterized stereotypes in operation works")
  public void testParameterizedStereotypeDoesCorrectBindingAndConnectorWorksForOperation() throws Exception {
    final CoreEvent muleEvent = flowRunner("testParameterizedStereotypeDoesCorrectBindingAndConnectorWorksForOperation").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("a value"));
  }
}
