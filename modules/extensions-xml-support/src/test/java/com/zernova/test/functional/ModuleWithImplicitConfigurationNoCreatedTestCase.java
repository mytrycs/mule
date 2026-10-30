/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;

import static org.junit.Assert.assertNull;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_DISABLE_XML_SDK_IMPLICIT_CONFIGURATION_CREATION;

import com.zernova.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class ModuleWithImplicitConfigurationNoCreatedTestCase extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Rule
  public SystemProperty disableXmlSdkImplicitConfiguration =
      new SystemProperty(MULE_DISABLE_XML_SDK_IMPLICIT_CONFIGURATION_CREATION, "true");

  @Override
  protected String getModulePath() {
    return "modules/module-global-element-default-params-with-no-use.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-global-element-default-params-with-no-use.xml";
  }

  @Test
  public void testXmlPropertyWithoutImplicitConfiguration() throws java.lang.Exception {
    String payload = (String) flowRunner("testSetXmlProperty").run().getMessage().getPayload().getValue();
    assertNull(payload);
  }
}
