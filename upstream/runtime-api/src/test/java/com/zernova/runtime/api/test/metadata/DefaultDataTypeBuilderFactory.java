/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.metadata;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.metadata.AbstractDataTypeBuilderFactory;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.DataTypeBuilder;

public final class DefaultDataTypeBuilderFactory extends AbstractDataTypeBuilderFactory {

  @Override
  protected DataTypeBuilder create() {
    return mock(DataTypeBuilder.class, RETURNS_DEEP_STUBS);
  }

  @Override
  protected DataTypeBuilder create(DataType dataType) {
    return mock(DataTypeBuilder.class, RETURNS_DEEP_STUBS);
  }
}
