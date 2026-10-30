/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.values;

import com.zernova.runtime.api.value.Value;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Set;

/**
 * Provider of {@link Value values}.
 *
 * @since 1.0
 * @see Value
 */
@MinZernovaVersion("4.1")
public interface ValueProvider {

  /**
   * Resolves and provides a {@link Set} of {@link Value values} which represents a set of possible and valid values for a
   * parameter.
   *
   * @return a {@link Set} of {@link Value values}.
   * @throws ValueResolvingException if an error occurs during the resolving
   */
  Set<Value> resolve() throws ValueResolvingException;

  /**
   * This is the id of the ValueProvider. It should not be repeated among {@link ValueProvider} in the same extension. Multiple
   * calls to this method must always return the same fixed value.
   *
   * @return name of the resolver
   * @since 1.4.0
   */
  default String getId() {
    return getClass().getName();
  }

}
