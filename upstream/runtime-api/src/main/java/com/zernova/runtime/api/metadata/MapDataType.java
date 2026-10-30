/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.metadata;

import com.zernova.api.annotation.NoImplement;

/**
 * A data type that represents a generified map.
 * <p>
 * When checked for compatibility the map type, the generic key type and the generic value type will be compared.
 *
 * @since 1.0
 */
@NoImplement
public interface MapDataType extends DataType {

  /**
   * Obtains the {@link DataType} of the keys in the map.
   *
   * @return the {@link DataType} of map keys.
   */
  DataType getKeyDataType();

  /**
   * Obtains the {@link DataType} of the values in the map.
   *
   * @return the {@link DataType} of map values.
   */
  DataType getValueDataType();

}
