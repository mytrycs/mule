/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.test.metadata.extension.model.animals.SwordFish;

public class TestBooleanMetadataResolver implements InputTypeResolver<Boolean> {

  public static final ClassTypeLoader loader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();

  @Override
  public String getCategoryName() {
    return "BooleanMetadataResolver";
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, Boolean key)
      throws MetadataResolvingException, ConnectionException {
    if (key) {
      return loader.load(SwordFish.class);
    }
    throw new MetadataResolvingException("false key", INVALID_METADATA_KEY);
  }
}
