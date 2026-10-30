/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.test.metadata.extension.model.animals.AnimalClade;
import com.zernova.test.metadata.extension.model.animals.Bear;
import com.zernova.test.metadata.extension.model.animals.SwordFish;

public class TestEnumMetadataResolver implements InputTypeResolver<AnimalClade> {

  @Override
  public String getCategoryName() {
    return "EnumMetadataResolver";
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, AnimalClade key)
      throws MetadataResolvingException, ConnectionException {
    switch (key) {
      case MAMMAL:
        return context.getTypeLoader().load(Bear.class);
      case FISH:
        return context.getTypeLoader().load(SwordFish.class);
      default:
        throw new MetadataResolvingException("Cannot identify animal", INVALID_METADATA_KEY);
    }
  }
}
