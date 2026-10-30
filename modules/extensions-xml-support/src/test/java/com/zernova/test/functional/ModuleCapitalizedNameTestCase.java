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

import org.junit.Test;

public class ModuleCapitalizedNameTestCase extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Override
  protected String getModulePath() {
    return "modules/module-capitalized-name.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-capitalized-name.xml";
  }

  @Test
  public void testSetPayloadHardcoded() throws Exception {
    final CoreEvent muleEvent = flowRunner("testSetPayloadHardcodedFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("hardcoded value"));
  }

  @Test
  public void testUnderscoreSetPayloadWithParameter() throws Exception {
    final CoreEvent muleEvent = flowRunner("testUnderscoreSetPayloadWithParameter").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("underscore property value, underscore parameter value"));
  }

  @Test
  public void testSetPayloadWithNameParameter() throws Exception {
    final CoreEvent muleEvent = flowRunner("testSetPayloadWithNameParameter").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("the name parameter"));
  }

}
