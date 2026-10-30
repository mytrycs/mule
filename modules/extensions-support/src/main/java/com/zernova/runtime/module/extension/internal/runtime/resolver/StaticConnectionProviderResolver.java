/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import java.util.Optional;

/**
 * An implementation of {@link ConnectionProviderValueResolver} that wraps a {@link StaticValueResolver}
 *
 * @since 4.0
 */
public class StaticConnectionProviderResolver<C> implements ConnectionProviderValueResolver<C> {

  private final ConnectionProvider<C> connectionProvider;
  private final ResolverSetResult resolverSetResult;

  public StaticConnectionProviderResolver(ConnectionProvider<C> connectionProvider,
                                          ResolverSetResult resolverSetResult) {
    this.connectionProvider = connectionProvider;
    this.resolverSetResult = resolverSetResult;
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> resolve(ValueResolvingContext context) throws ZernovaException {
    return new Pair<>(connectionProvider, resolverSetResult);
  }

  @Override
  public boolean isDynamic() {
    return false;
  }

  @Override
  public Optional<ResolverSet> getResolverSet() {
    return Optional.empty();
  }
}
