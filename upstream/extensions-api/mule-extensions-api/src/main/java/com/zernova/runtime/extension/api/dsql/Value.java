/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.dsql;

import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Represents a value for a query field comparison.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public abstract class Value<T> {

  private T value;

  protected Value(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(getValue());
  }
}
