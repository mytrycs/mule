/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import com.zernova.runtime.api.message.Message;

import org.junit.Test;

public class ConnectionProviderStereotypeTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "custom-os-config.xml";
  }

  @Test
  public void storeInCustomStore() throws Exception {
    Message message = flowRunner("customStore").run().getMessage();
    assertThat(message.getPayload().getValue(), equalTo("Extend all the things!"));
  }
}
