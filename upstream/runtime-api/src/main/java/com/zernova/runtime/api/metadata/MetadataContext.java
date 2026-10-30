/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.metadata;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.lifecycle.Disposable;

import java.util.Optional;

/**
 * Metadata resolving context, provides access to the Config and Connection used during metadata fetch invocation.
 *
 * @since 1.0
 */
@NoImplement
public interface MetadataContext extends Disposable {

  /**
   * @param <C> Configuration type
   * @return Optional instance of the configuration related to the component
   * @deprecated metadata resolution should not require the configuration.
   */
  @Deprecated
  <C> Optional<C> getConfig();

  /**
   * Retrieves the connection for the related a component and configuration
   *
   * @param <C> Connection type
   * @return Optional connection instance of {@param <C>} type for the component.
   * @throws ConnectionException when not valid connection is found for the related component and configuration
   */
  <C> Optional<C> getConnection() throws ConnectionException;

  /**
   * @return the {@link ClassTypeLoader} for the current {@link MetadataContext}.
   */
  ClassTypeLoader getTypeLoader();

  /**
   * @return the {@link BaseTypeBuilder} for the current {@link MetadataContext}.
   */
  BaseTypeBuilder getTypeBuilder();

  /**
   * @return the {@link MetadataCache} associated with the {@link MetadataContext}.
   */
  MetadataCache getCache();

  /**
   * <b>NOTE:</b> Experimental feature. Backwards compatibility is not guaranteed.
   *
   * @return For scope components, describes the inner chain's input and output types.
   * @since 1.7
   */
  @Experimental
  Optional<ScopeOutputMetadataContext> getScopeOutputMetadataContext();

  /**
   * <b>NOTE:</b> Experimental feature. Backwards compatibility is not guaranteed.
   *
   * @return For router components, describes the inner routes input and output types.
   * @since 1.7
   */
  @Experimental
  Optional<RouterOutputMetadataContext> getRouterOutputMetadataContext();
}
