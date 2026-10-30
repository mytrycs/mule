/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import com.zernova.tck.testmodels.fruit.Banana;

import org.junit.Test;

public class ConfigAliasTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "vegan-config-alias.xml";
  }

  @Test
  public void parseConnectionProviderWithAlias() throws Exception {
    Banana connection = (Banana) runFlow("alias").getMessage().getPayload().getValue();
    assertThat(connection, is(notNullValue()));
    assertThat(connection.getOrigin(), is("Brazil"));
  }
}
