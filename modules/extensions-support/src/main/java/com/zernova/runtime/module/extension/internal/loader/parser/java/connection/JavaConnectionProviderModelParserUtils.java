/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.connection;

import com.zernova.runtime.api.connection.CachedConnectionProvider;
import com.zernova.runtime.api.connection.PoolingConnectionProvider;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.module.extension.api.loader.java.type.ConnectionProviderElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.sdk.api.connectivity.ConnectionProvider;
import com.zernova.sdk.api.connectivity.TransactionalConnection;

/**
 * Utilities for parsing Java defined {@link ConnectionProviderModel} instances
 *
 * @since 4.5.0
 */
public final class JavaConnectionProviderModelParserUtils {

  /**
   * @param element a {@link ConnectionProviderElement}
   * @return whether the {@code element} represents a pooling provider
   */
  public static boolean isPoolingConnectionProvider(ConnectionProviderElement element) {
    return element.isAssignableTo(PoolingConnectionProvider.class)
        || element.isAssignableTo(com.zernova.sdk.api.connectivity.PoolingConnectionProvider.class);
  }

  /**
   * @param element a {@link ConnectionProviderElement}
   * @return whether the {@code element} represents a caching provider
   */
  public static boolean isCachedConnectionProvider(ConnectionProviderElement element) {
    return element.isAssignableTo(CachedConnectionProvider.class)
        || element.isAssignableTo(com.zernova.sdk.api.connectivity.CachedConnectionProvider.class);
  }

  /**
   * @param element a {@link ConnectionProviderElement}
   * @return whether the {@code element} represents a provider defined through the sdk-api
   */
  public static boolean isDefinedThroughSdkApi(ConnectionProviderElement element) {
    return element.isAssignableTo(ConnectionProvider.class);
  }

  /**
   * @param connectionType the connection type
   * @return whether the given {@code connectionType} represents a transactional connection
   */
  public static boolean isTransactional(Type connectionType) {
    return connectionType.isAssignableTo(TransactionalConnection.class);
  }

  private JavaConnectionProviderModelParserUtils() {}
}
