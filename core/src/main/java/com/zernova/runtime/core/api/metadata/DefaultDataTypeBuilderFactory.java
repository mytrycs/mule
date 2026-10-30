/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.metadata;

import com.zernova.runtime.api.metadata.AbstractDataTypeBuilderFactory;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.DataTypeBuilder;
import com.zernova.runtime.core.privileged.metadata.DefaultDataTypeBuilder;

public final class DefaultDataTypeBuilderFactory extends AbstractDataTypeBuilderFactory {

  @Override
  protected DataTypeBuilder create() {
    return new DefaultDataTypeBuilder();
  }

  @Override
  protected DataTypeBuilder create(DataType dataType) {
    return new DefaultDataTypeBuilder(dataType);
  }
}
