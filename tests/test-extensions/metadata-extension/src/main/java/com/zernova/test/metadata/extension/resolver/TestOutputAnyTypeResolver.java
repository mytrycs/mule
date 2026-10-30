/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;

public class TestOutputAnyTypeResolver implements OutputTypeResolver<String> {

  public static final String TEST_OUTPUT_ANY_TYPE_RESOLVER = "TestOutputAnyTypeResolver";
  public static final String METADATA_EXTENSION_RESOLVER = "MetadataExtensionResolver";

  @Override
  public MetadataType getOutputType(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    return BaseTypeBuilder.create(JAVA).anyType().build();
  }

  @Override
  public String getCategoryName() {
    return METADATA_EXTENSION_RESOLVER;
  }

  @Override
  public String getResolverName() {
    return TEST_OUTPUT_ANY_TYPE_RESOLVER;
  }
}
