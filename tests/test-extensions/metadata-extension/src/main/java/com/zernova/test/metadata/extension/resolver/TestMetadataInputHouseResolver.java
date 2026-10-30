/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static com.zernova.test.metadata.extension.resolver.TestMetadataResolverUtils.getHouseMetadata;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;

public class TestMetadataInputHouseResolver implements InputTypeResolver<String> {

  public static String TEST_INPUT_HOUSE_RESOLVER = "testInputHouseResolver";

  @Override
  public String getResolverName() {
    return TEST_INPUT_HOUSE_RESOLVER;
  }

  @Override
  public String getCategoryName() {
    return "TestResolvers";
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    return getHouseMetadata();
  }
}
