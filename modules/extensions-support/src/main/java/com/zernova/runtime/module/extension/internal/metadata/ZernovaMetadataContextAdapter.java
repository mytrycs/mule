/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import static java.util.Optional.empty;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.ScopeOutputMetadataContext;
import com.zernova.runtime.api.metadata.RouterOutputMetadataContext;
import com.zernova.runtime.module.extension.internal.metadata.SdkMetadataContextAdapter.SdkRouterOutputMetadataContextAdapter;
import com.zernova.runtime.module.extension.internal.metadata.SdkMetadataContextAdapter.SdkScopeOutputMetadataContext;
import com.zernova.sdk.api.metadata.MetadataContext;

import java.util.Optional;

public class ZernovaMetadataContextAdapter implements com.zernova.runtime.api.metadata.MetadataContext {

  private final MetadataContext delegate;

  public ZernovaMetadataContextAdapter(MetadataContext delegate) {
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
  public com.zernova.runtime.api.metadata.MetadataCache getCache() {
    return new ZernovaMetadataCacheAdapter(delegate.getCache());
  }

  @Override
  public Optional<ScopeOutputMetadataContext> getScopeOutputMetadataContext() {
    return delegate.getScopeOutputMetadataContext()
        .map(ctx -> ((SdkScopeOutputMetadataContext) ctx).getDelegate());
  }

  @Override
  public Optional<RouterOutputMetadataContext> getRouterOutputMetadataContext() {
    return delegate.getRouterOutputMetadataContext()
        .map(ctx -> ((SdkRouterOutputMetadataContextAdapter) ctx).getDelegate());
  }

  @Override
  public <C> Optional<C> getConfig() {
    return empty();
  }
}
