/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;

import static java.util.Optional.of;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.extension.api.runtime.config.ConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.config.BaseConnectionProviderObjectBuilder;

import java.util.Optional;

/**
 * A {@link ValueResolver} specialization for producing {@link ConnectionProvider} instances through a
 * {@link BaseConnectionProviderObjectBuilder}
 *
 * @since 4.0
 */
public class ConnectionProviderResolver<C> extends AbstractComponent
    implements ConnectionProviderValueResolver<C>, Initialisable, Startable {

  private final BaseConnectionProviderObjectBuilder<C> objectBuilder;
  private final ObjectBuilderValueResolver<Pair<ConnectionProvider<C>, ResolverSetResult>> valueResolver;
  private final ResolverSet resolverSet;

  /**
   * Creates a new instance
   *
   * @param objectBuilder an object builder to instantiate the {@link ConnectionProvider}
   */
  public ConnectionProviderResolver(BaseConnectionProviderObjectBuilder<C> objectBuilder, ResolverSet resolverSet,
                                    ZernovaContext muleContext) {
    this.objectBuilder = objectBuilder;
    this.valueResolver = new ObjectBuilderValueResolver<>(objectBuilder, muleContext);
    this.resolverSet = resolverSet;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> resolve(ValueResolvingContext context) throws ZernovaException {
    return valueResolver.resolve(context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return valueResolver.isDynamic();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ResolverSet> getResolverSet() {
    return of(resolverSet);
  }

  @Override
  public Optional<ConnectionProviderObjectBuilder<C>> getObjectBuilder() {
    return of(objectBuilder);
  }

  public void setOwnerConfigName(String ownerConfigName) {
    objectBuilder.setOwnerConfigName(ownerConfigName);
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(resolverSet);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(objectBuilder);
  }
}
