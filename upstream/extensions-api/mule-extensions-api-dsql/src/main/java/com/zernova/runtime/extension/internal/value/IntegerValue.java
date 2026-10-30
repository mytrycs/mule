/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.value;

import com.zernova.runtime.extension.api.dsql.Value;

/**
 * {@link Value} implementation, that represents an integer.
 *
 * @since 1.0
 */
public class IntegerValue extends Value<Integer> {

  public IntegerValue(Integer value) {
    super(value);
  }

  public static IntegerValue fromLiteral(String literal) {
    return new IntegerValue(Integer.parseInt(literal));
  }
}
