/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.AnyType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.resolving.OutputStaticTypeResolver;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;

public class TucoMetadataResolver extends OutputStaticTypeResolver {

  private static final AnyType ANY_TYPE = BaseTypeBuilder.create(JAVA).anyType().build();

  @Override
  public String getCategoryName() {
    return "TUCO";
  }

  @Override
  public MetadataType getStaticMetadata() {
    return ANY_TYPE;
  }

}
