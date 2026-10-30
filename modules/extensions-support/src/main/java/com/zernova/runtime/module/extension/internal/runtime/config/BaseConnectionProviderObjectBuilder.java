/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.core.api.retry.ReconnectionConfig.defaultReconnectionConfig;
import static com.zernova.runtime.core.internal.connection.ConnectionUtils.getInjectionTarget;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.connection.SdkConnectionProviderAdapter.from;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getConnectionProviderFactory;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.module.extension.api.runtime.config.ConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.internal.loader.parser.java.connection.SdkConnectionProviderAdapter;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ResolverSetBasedObjectBuilder;
import com.zernova.runtime.module.extension.internal.util.ValueSetter;

import java.util.List;

/**
 * Implementation of {@link ResolverSetBasedObjectBuilder} which produces instances of {@link ConnectionProviderModel}
 *
 * @since 4.0
 */
public abstract class BaseConnectionProviderObjectBuilder<C>
    extends ResolverSetBasedObjectBuilder<Pair<ConnectionProvider<C>, ResolverSetResult>>
    implements ConnectionProviderObjectBuilder<C> {

  protected final ConnectionProviderModel providerModel;
  protected final ReconnectionConfig reconnectionConfig;
  protected final PoolingProfile poolingProfile;
  protected final ExtensionModel extensionModel;
  protected final ZernovaContext muleContext;

  private volatile boolean firstBuild = true;
  protected String ownerConfigName;

  /**
   * Creates a new instances which produces instances based on the given {@code providerModel} and {@code resolverSet}
   *
   * @param providerModel the {@link ConnectionProviderModel} which describes the instances to be produced
   * @param resolverSet   a {@link ResolverSet} to populate the values
   */
  public BaseConnectionProviderObjectBuilder(ConnectionProviderModel providerModel,
                                             ResolverSet resolverSet,
                                             ExtensionModel extensionModel,
                                             ExpressionManager expressionManager,
                                             ZernovaContext muleContext) {
    this(providerModel, resolverSet, null, null, extensionModel, expressionManager, muleContext);
  }

  public BaseConnectionProviderObjectBuilder(Class<?> prototypeClass,
                                             ConnectionProviderModel providerModel,
                                             ResolverSet resolverSet,
                                             PoolingProfile poolingProfile,
                                             ReconnectionConfig reconnectionConfig,
                                             ExtensionModel extensionModel,
                                             ExpressionManager expressionManager,
                                             ZernovaContext muleContext) {
    super(prototypeClass, providerModel, resolverSet, expressionManager, muleContext);
    this.providerModel = providerModel;
    this.poolingProfile = poolingProfile;
    this.extensionModel = requireNonNull(extensionModel);
    this.muleContext = muleContext;
    this.reconnectionConfig = computeReconnectionConfig(reconnectionConfig);
  }

  public BaseConnectionProviderObjectBuilder(ConnectionProviderModel providerModel,
                                             ResolverSet resolverSet,
                                             PoolingProfile poolingProfile,
                                             ReconnectionConfig reconnectionConfig,
                                             ExtensionModel extensionModel,
                                             ExpressionManager expressionManager,
                                             ZernovaContext muleContext) {
    this(getConnectionProviderFactory(providerModel).getObjectType(), providerModel, resolverSet, poolingProfile,
         reconnectionConfig, extensionModel, expressionManager, muleContext);
  }

  private ReconnectionConfig computeReconnectionConfig(ReconnectionConfig reconnectionConfig) {
    return reconnectionConfig != null ? reconnectionConfig : defaultReconnectionConfig();
  }

  public BaseConnectionProviderObjectBuilder(ConnectionProviderModel providerModel,
                                             Class<?> prototypeClass,
                                             ResolverSet resolverSet,
                                             PoolingProfile poolingProfile,
                                             ReconnectionConfig reconnectionConfig,
                                             ExtensionModel extensionModel,
                                             ExpressionManager expressionManager,
                                             ZernovaContext muleContext) {
    super(prototypeClass, providerModel, resolverSet, expressionManager, muleContext);
    this.providerModel = providerModel;
    this.poolingProfile = poolingProfile;
    this.extensionModel = extensionModel;
    this.muleContext = muleContext;
    this.reconnectionConfig = computeReconnectionConfig(reconnectionConfig);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Pair<ConnectionProvider<C>, ResolverSetResult> instantiateObject() {
    return new Pair<>(getConnectionProviderFactory(providerModel).newInstance(), null);
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> build(ResolverSetResult result) throws ZernovaException {
    final ConnectionProvider<C> value = from(instantiateObject().getFirst());
    Object injectionTarget = getInjectionTarget(value);

    if (firstBuild) {
      synchronized (this) {
        if (firstBuild) {
          singleValueSetters = super.createSingleValueSetters(injectionTarget.getClass(), resolverSet);
          firstBuild = false;
        }
      }
    }

    populate(result, injectionTarget);
    return new Pair<>(value, result);
  }

  /**
   * In order to support {@link com.zernova.sdk.api.connectivity.ConnectionProvider} instances, introspection needs to be deferred to
   * the actual instantiation process so that {@link SdkConnectionProviderAdapter} can be unwrapped.
   * <p>
   * Therefore, this method always returns an empty list so that no introspection happens on the setup of this builder but
   * deferred to the first execution of the {@link #build(ResolverSetResult)} method.
   */
  @Override
  protected List<ValueSetter> createSingleValueSetters(Class<?> prototypeClass, ResolverSet resolverSet) {
    return emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return resolverSet.isDynamic();
  }

  public void setOwnerConfigName(String ownerConfigName) {
    this.ownerConfigName = ownerConfigName;
  }
}
