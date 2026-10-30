/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.typed.value;

import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.test.typed.value.extension.extension.TypedValueExtension;
import com.zernova.test.typed.value.extension.extension.TypedValueSource;

import org.junit.After;
import org.junit.Test;

public class TypedValueParameterOnConfigTestCase extends AbstractTypedValueTestCase {

  @Override
  protected String[] getConfigFiles() {
    return new String[] {"typed-value-on-config-config.xml"};
  }

  @After
  public void cleanUp() {
    TypedValueSource.onSuccessValue = null;
  }

  @Test
  public void typedValueOnDynamicConfig() throws Exception {
    TypedValueExtension extension =
        (TypedValueExtension) flowRunner("typedValueOnDynamicConfig").run().getMessage().getPayload().getValue();

    assertTypedValue(extension.getStringTypedValue(), "JsonStringElement", MediaType.ANY, null);
    assertTypedValue(extension.getDifferedDoor().getAddress(), "address", MediaType.ANY, null);
  }

  @Test
  public void typedValueOnStaticConfig() throws Exception {
    TypedValueExtension extension =
        (TypedValueExtension) flowRunner("typedValueOnStaticConfig").run().getMessage().getPayload().getValue();

    assertTypedValue(extension.getStringTypedValue(), "string", MediaType.ANY, null);
    assertTypedValue(extension.getDifferedDoor().getAddress(), "address", MediaType.ANY, null);
  }
}
