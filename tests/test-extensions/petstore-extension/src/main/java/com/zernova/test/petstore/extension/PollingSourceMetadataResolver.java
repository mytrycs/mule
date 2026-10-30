/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;

public class PollingSourceMetadataResolver implements OutputTypeResolver {

  @Override
  public MetadataType getOutputType(MetadataContext context, Object key) {
    return BaseTypeBuilder.create(JAVA).stringType().build();
  }

  @Override
  public String getCategoryName() {
    return "PetStore";
  }
}
