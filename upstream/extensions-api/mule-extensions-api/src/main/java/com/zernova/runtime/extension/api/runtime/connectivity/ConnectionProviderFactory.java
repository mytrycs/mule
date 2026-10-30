/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.connectivity;


import com.zernova.runtime.api.connection.ConnectionProvider;

/**
 * Creates instances of {@link ConnectionProvider} for the generic {@code Config} and {@code Connection} types.
 * <p>
 * Instances are thread-safe and reusable.
 *
 * @param <T> the generic type for the connections that the returned {@link ConnectionProvider providers} produce
 * @since 1.0
 */
public interface ConnectionProviderFactory<T> {

  /**
   * @return a new {@link ConnectionProvider}
   */
  ConnectionProvider<T> newInstance();

  /**
   * Returns the concrete type of the object to be returned by this instance
   *
   * @return a {@link Class}
   */
  Class<? extends ConnectionProvider> getObjectType();
}
