/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.config.DefaultImplicitConnectionProviderFactory;
import com.zernova.runtime.module.extension.internal.runtime.config.ImplicitConnectionProviderFactory;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Optional;

/**
 * Uses a {@link ImplicitConnectionProviderFactory} to create an implicit {@link ConnectionProvider}.
 * <p>
 * This is a static {@link ValueResolver}. The {@link ConnectionProvider} is created the first time the
 * {@link #resolve(ValueResolvingContext)} method is invoked on {@code this} instance. Subsequent invocations will return the same
 * instance.
 * <p>
 * This class is thread-safe
 *
 * @since 4.0
 */
public final class ImplicitConnectionProviderValueResolver<C> implements ConnectionProviderValueResolver<C> {

  private final ImplicitConnectionProviderFactory implicitConnectionProviderFactory;
  private final String configName;

  public ImplicitConnectionProviderValueResolver(String name,
                                                 ExtensionModel extensionModel,
                                                 ConfigurationModel configurationModel,
                                                 ReflectionCache reflectionCache,
                                                 ExpressionManager expressionManager,
                                                 ZernovaContext muleContext) {
    configName = name;
    implicitConnectionProviderFactory = new DefaultImplicitConnectionProviderFactory(extensionModel,
                                                                                     configurationModel,
                                                                                     reflectionCache,
                                                                                     expressionManager,
                                                                                     muleContext);
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> resolve(ValueResolvingContext context) {
    return implicitConnectionProviderFactory.createImplicitConnectionProvider(configName, context.getEvent());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return implicitConnectionProviderFactory.isDynamic();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ResolverSet> getResolverSet() {
    return implicitConnectionProviderFactory.getResolverSet();
  }
}
