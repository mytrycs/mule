/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.connector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class PetStoreInputStreamContentTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "petstore-input-stream-content-parameter.xml";
  }

  @Test
  public void parseInputStreamContentParameter() throws Exception {
    String fish = (String) flowRunner("getFish")
        .withVariable("herring", new ByteArrayInputStream("herring".getBytes()))
        .run().getMessage().getPayload().getValue();

    assertThat(fish, is("goldfish herring"));
  }
}
