/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;


import static org.hamcrest.Matchers.is;

import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.core.api.event.CoreEvent;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ModuleEchoTestCase extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Override
  protected String getModulePath() {
    return "modules/module-echo.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-echo.xml";
  }

  @Test
  public void verifyDataType() throws Exception {
    CoreEvent muleEvent = flowRunner("test").withMediaType(MediaType.JSON).withPayload("{ \"name\":\"Emiliano\" }").run();
    MatcherAssert.assertThat(muleEvent.getMessage().getPayload().getValue(), is(MediaType.JSON.toRfcString()));
  }

}
