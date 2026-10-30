/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getMuleVersion;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.supportsConnectivity;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.CONFIGURATION_MODEL_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.injectFields;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationState;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reusable and thread-safe factory that creates instances of {@link ConfigurationInstance}
 *
 * @param <T> the generic type of the returned {@link ConfigurationInstance} instances
 * @since 4.0
 */
public final class ConfigurationInstanceFactory<T> {

  private final ConfigurationModel configurationModel;
  private final ConfigurationObjectBuilder<T> configurationObjectBuilder;
  private final boolean requiresConnection;
  private final ExpressionManager expressionManager;
  private final ZernovaContext muleContext;

  /**
   * Creates a new instance which provides instances derived from the given {@code configurationModel} and {@code resolverSet}
   *
   * @param extensionModel     the {@link ExtensionModel} that owns the {@code configurationModel}
   * @param configurationModel the {@link ConfigurationModel} that describes the configurations to be created
   * @param resolverSet        the {@link ResolverSet} which provides the values for the configuration's parameters
   * @param expressionManager  the {@link ExpressionManager} used to create a session used to evaluate the attributes.
   * @param context            the current {@link ZernovaContext}
   */
  public ConfigurationInstanceFactory(ExtensionModel extensionModel,
                                      ConfigurationModel configurationModel,
                                      ResolverSet resolverSet,
                                      ExpressionManager expressionManager,
                                      ZernovaContext context) {
    this.configurationModel = configurationModel;
    this.expressionManager = expressionManager;
    this.muleContext = context;
    configurationObjectBuilder = new ConfigurationObjectBuilder<>(configurationModel, resolverSet, expressionManager, context);
    requiresConnection = supportsConnectivity(extensionModel, configurationModel);
  }

  /**
   * Creates a new instance using the given {@code event} to obtain the configuration's parameter values.
   * <p>
   * This method overload allows specifying a {@link ValueResolver} to provide the {@link ConnectionProvider} that the
   * configuration will use to obtain connections. If the connection does not need such a concept you can provide a {@code null}
   *
   * @param name     the name of the configuration to return
   * @param event    the current {@link CoreEvent}
   * @param resolver a {@link ValueResolver} to provide the {@link ConnectionProvider} or {@code null}
   * @return a {@link ConfigurationInstance}
   * @throws ZernovaException if an error is encountered
   */
  public <C> ConfigurationInstance createConfiguration(String name, CoreEvent event, ConnectionProviderValueResolver<C> resolver)
      throws ZernovaException {

    Optional<Pair<ConnectionProvider<C>, ResolverSetResult>> connectionProvider;

    Pair<T, ResolverSetResult> configValue = createConfigurationInstance(name, event);
    if (requiresConnection) {
      try (ValueResolvingContext ctx = ValueResolvingContext.builder(event, expressionManager)
          .withProperty(CONFIGURATION_MODEL_PROPERTY_NAME, configurationModel).build()) {
        connectionProvider = ofNullable(resolver.resolve(ctx));
      }
    } else {
      connectionProvider = empty();
    }

    return new LifecycleAwareConfigurationInstance(name,
                                                   configurationModel,
                                                   configValue.getFirst(),
                                                   createState(configValue.getSecond(), connectionProvider),
                                                   connectionProvider.map(Pair::getFirst));
  }

  /**
   * Creates a new instance using the given {@code resolverSetResult} to obtain the configuration's parameter values
   *
   * @param name                       the name of the configuration to return
   * @param configValues               the {@link ResolverSetResult} with the evaluated config parameters values
   * @param event                      the current {@link CoreEvent}
   * @param connectionProviderResolver an optional resolver to obtain a {@link ConnectionProvider}
   * @return a {@link ConfigurationInstance}
   * @throws ZernovaException if an error is encountered
   */
  public <C> ConfigurationInstance createConfiguration(String name,
                                                       ResolverSetResult configValues,
                                                       CoreEvent event,
                                                       Optional<ConnectionProviderValueResolver<C>> connectionProviderResolver)
      throws ZernovaException {

    Pair<T, ResolverSetResult> configValue = createConfigurationInstance(name, configValues);

    Optional<Pair<ConnectionProvider<C>, ResolverSetResult>> connectionProvider;

    if (requiresConnection && connectionProviderResolver.isPresent()) {
      ConnectionProviderValueResolver<C> resolver = connectionProviderResolver.get();
      try (ValueResolvingContext cxt = ValueResolvingContext.builder(event, expressionManager).build()) {
        connectionProvider = ofNullable(resolver.resolve(cxt));
      }
    } else {
      connectionProvider = empty();
    }

    return new LifecycleAwareConfigurationInstance(name,
                                                   configurationModel,
                                                   configValue.getFirst(),
                                                   createState(configValue.getSecond(), connectionProvider),
                                                   connectionProvider.map(Pair::getFirst));
  }

  /**
   * Creates a new instance using the given {@code configValues} and {@code connectionProviderValues} to obtain the
   * configuration's parameter values
   *
   * @param name                     the name of the configuration to return
   * @param configValues             the {@link ResolverSetResult} with the evaluated config parameters values
   * @param event                    the current {@link CoreEvent}
   * @param resolver                 a resolver to obtain a {@link ConnectionProvider}
   * @param connectionProviderValues e {@link ResolverSetResult} with the evaluated connection parameters values
   * @return a {@link ConfigurationInstance}
   * @throws ZernovaException if an error is encountered
   */
  public <C> ConfigurationInstance createConfiguration(String name,
                                                       ResolverSetResult configValues,
                                                       CoreEvent event,
                                                       ConnectionProviderValueResolver<C> resolver,
                                                       ResolverSetResult connectionProviderValues)
      throws ZernovaException {
    Pair<T, ResolverSetResult> configValue = createConfigurationInstance(name, configValues);

    Optional<Pair<ConnectionProvider<C>, ResolverSetResult>> connectionProvider = empty();

    if (requiresConnection && resolver != null) {
      if (resolver.getObjectBuilder().isPresent()) {
        connectionProvider = ofNullable(resolver.getObjectBuilder().get().build(connectionProviderValues));
      }

      if (!connectionProvider.isPresent()) {
        try (ValueResolvingContext context = ValueResolvingContext.builder(event, expressionManager).build()) {
          connectionProvider = ofNullable(resolver.resolve(context));
        }
      }
    }

    return new LifecycleAwareConfigurationInstance(name,
                                                   configurationModel,
                                                   configValue.getFirst(),
                                                   createState(configValue.getSecond(), connectionProvider),
                                                   connectionProvider.map(Pair::getFirst));
  }

  private Pair<T, ResolverSetResult> createConfigurationInstance(String name, ResolverSetResult resolverSetResult)
      throws ZernovaException {
    Pair<T, ResolverSetResult> config = configurationObjectBuilder.build(resolverSetResult);
    ZernovaVersion muleVersion = getMuleVersion();
    injectFields(configurationModel, config.getFirst(), name, muleContext.getConfiguration().getDefaultEncoding(), muleVersion);


    return config;
  }

  private Pair<T, ResolverSetResult> createConfigurationInstance(String name, CoreEvent event) throws ZernovaException {
    try (ValueResolvingContext context = ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build()) {
      Pair<T, ResolverSetResult> config = configurationObjectBuilder.build(context);
      ZernovaVersion muleVersion = getMuleVersion();
      injectFields(configurationModel, config.getFirst(), name, muleContext.getConfiguration().getDefaultEncoding(), muleVersion);
      return config;
    }
  }

  private <C> ConfigurationState createState(ResolverSetResult configValues,
                                             Optional<Pair<ConnectionProvider<C>, ResolverSetResult>> connectionProvider) {
    return new ImmutableConfigurationState(
                                           nullSafeMap(configValues),
                                           connectionProvider.map(p -> nullSafeMap(p.getSecond()))
                                               .orElseGet(Collections::emptyMap));
  }

  private Map<String, Object> nullSafeMap(ResolverSetResult result) {
    return result.asMap().entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
  }
}
