/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.message.api.MessageMetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.sdk.api.metadata.MetadataCache;
import com.zernova.sdk.api.metadata.MetadataContext;
import com.zernova.sdk.api.metadata.RouterOutputMetadataContext;
import com.zernova.sdk.api.metadata.ScopeOutputMetadataContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Adapts {@code mule-api}'s {@link com.zernova.runtime.api.metadata.MetadataContext} into a {@code sdk-api} {@link MetadataContext}
 *
 * @since 4.7.0
 */
public class SdkMetadataContextAdapter implements MetadataContext {

  private final com.zernova.runtime.api.metadata.MetadataContext delegate;

  public SdkMetadataContextAdapter(com.zernova.runtime.api.metadata.MetadataContext delegate) {
    this.delegate = delegate;
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }

  @Override
  public <C> Optional<C> getConnection() throws ConnectionException {
    return delegate.getConnection();
  }

  @Override
  public ClassTypeLoader getTypeLoader() {
    return delegate.getTypeLoader();
  }

  @Override
  public BaseTypeBuilder getTypeBuilder() {
    return delegate.getTypeBuilder();
  }

  @Override
  public MetadataCache getCache() {
    return new SdkMetadataCacheAdapter(delegate.getCache());
  }

  @Override
  public Optional<ScopeOutputMetadataContext> getScopeOutputMetadataContext() {
    return delegate.getScopeOutputMetadataContext().map(SdkScopeOutputMetadataContext::new);
  }

  @Override
  public Optional<RouterOutputMetadataContext> getRouterOutputMetadataContext() {
    return delegate.getRouterOutputMetadataContext().map(SdkRouterOutputMetadataContextAdapter::new);
  }

  static class SdkScopeOutputMetadataContext implements ScopeOutputMetadataContext {

    private final com.zernova.runtime.api.metadata.ScopeOutputMetadataContext delegate;

    private SdkScopeOutputMetadataContext(com.zernova.runtime.api.metadata.ScopeOutputMetadataContext delegate) {
      this.delegate = delegate;
    }

    @Override
    public Supplier<MessageMetadataType> getInnerChainOutputMessageType() {
      return delegate.getInnerChainOutputMessageType();
    }

    @Override
    public Supplier<MessageMetadataType> getScopeInputMessageType() {
      return delegate.getScopeInputMessageType();
    }

    com.zernova.runtime.api.metadata.ScopeOutputMetadataContext getDelegate() {
      return delegate;
    }
  }

  static class SdkRouterOutputMetadataContextAdapter implements RouterOutputMetadataContext {

    private final com.zernova.runtime.api.metadata.RouterOutputMetadataContext delegate;

    public SdkRouterOutputMetadataContextAdapter(com.zernova.runtime.api.metadata.RouterOutputMetadataContext delegate) {
      this.delegate = delegate;
    }

    @Override
    public Map<String, Supplier<MessageMetadataType>> getRouteOutputMessageTypes() {
      return delegate.getRouteOutputMessageTypes();
    }

    @Override
    public Supplier<MessageMetadataType> getRouterInputMessageType() {
      return delegate.getRouterInputMessageType();
    }

    com.zernova.runtime.api.metadata.RouterOutputMetadataContext getDelegate() {
      return delegate;
    }
  }
}
