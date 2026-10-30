/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.transformer;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.metadata.DataType;

import java.util.List;

/**
 * Resolves a conversion from one data type to another returning a converter {@link Transformer} that is able to convert the data.
 */
@NoImplement
public interface DataTypeConversionResolver {

  Transformer resolve(DataType sourceType, List<DataType> targetDataTypes);
}
