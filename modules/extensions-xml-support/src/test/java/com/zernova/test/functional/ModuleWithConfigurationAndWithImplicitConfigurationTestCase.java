/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_DISABLE_XML_SDK_IMPLICIT_CONFIGURATION_CREATION;
import static com.zernova.test.allure.AllureConstants.XmlSdk.XML_SDK;

import org.junit.Rule;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.test.petstore.extension.PetStoreClient;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(XML_SDK)
public class ModuleWithConfigurationAndWithImplicitConfigurationTestCase
    extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Rule
  public SystemProperty disableXmlSdkImplicitConfiguration =
      new SystemProperty(MULE_DISABLE_XML_SDK_IMPLICIT_CONFIGURATION_CREATION, "false");

  @Override
  protected String getModulePath() {
    return "modules/module-global-element-default-params.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-global-element-default-params-with-config-and-implicit-config.xml";
  }

  @Test
  public void testDoGetClient() throws Exception {
    PetStoreClient client = (PetStoreClient) flowRunner("testDoGetClient")
        .run().getMessage().getPayload().getValue();
    assertThat(client.getUsername(), is("john"));
    assertThat(client.getPassword(), is("notDoe"));
  }

  @Test
  public void testDoGetClientCustomProperties() throws Exception {
    PetStoreClient client = (PetStoreClient) flowRunner("testDoGetClientCustomProperties")
        .run().getMessage().getPayload().getValue();
    assertThat(client.getUsername(), is("john"));
    assertThat(client.getPassword(), is("changeit"));
  }


}
