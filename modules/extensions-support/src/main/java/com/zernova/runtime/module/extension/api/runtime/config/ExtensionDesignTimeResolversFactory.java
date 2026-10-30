/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.runtime.config;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.metadata.MetadataCache;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.RouterOutputMetadataContext;
import com.zernova.runtime.api.metadata.ScopeOutputMetadataContext;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.tooling.metadata.MetadataMediator;
import com.zernova.runtime.module.extension.api.tooling.sampledata.SampleDataProviderMediator;
import com.zernova.runtime.module.extension.api.tooling.valueprovider.ValueProviderMediator;
import com.zernova.runtime.module.extension.internal.runtime.config.ResolverSetBasedParameterResolver;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides a way to create resolvers, configProviders and mediators to be used in design time.
 *
 * @since 4.8
 */
public interface ExtensionDesignTimeResolversFactory {

  /**
   * Creates a {@link ConnectionProviderValueResolver} for the provided parameters.
   *
   * @return a new {@link ConnectionProviderValueResolver}
   * @param <C> the generic type of the provider's connection object
   * @throws ZernovaException if the resolver cannot be created
   */
  <C> ConnectionProviderValueResolver<C> createConnectionProviderResolver(ConnectionProviderModel connectionProviderModel,
                                                                          ComponentParameterization componentParameterization,
                                                                          PoolingProfile poolingProfile,
                                                                          ReconnectionConfig reconnectionConfig,
                                                                          ExtensionModel extensionModel,
                                                                          ConfigurationProperties configurationProperties,
                                                                          String parametersOwner,
                                                                          DslSyntaxResolver dslSyntaxResolver)
      throws ZernovaException;

  /**
   * Creates a {@link ConfigurationProvider} for the provided parameters.
   *
   * @return a new {@link ConfigurationProvider}
   * @throws ZernovaException if the resolver cannot be created
   */
  ConfigurationProvider createConfigurationProvider(ExtensionModel extensionModel,
                                                    ConfigurationModel configurationModel,
                                                    String configName,
                                                    Map<String, Object> parameters,
                                                    Optional<ExpirationPolicy> expirationPolicy,
                                                    Optional<ConnectionProviderValueResolver> connectionProviderResolver,
                                                    ConfigurationProviderFactory configurationProviderFactory,
                                                    String parametersOwner,
                                                    DslSyntaxResolver dslSyntaxResolver,
                                                    ClassLoader extensionClassLoader);

  /**
   * Creates a {@link ConnectionProvider} for the provided parameters.
   *
   * @return a new {@link ConnectionProvider}
   * @throws ZernovaException if the resolver cannot be created
   */
  <C> ConnectionProvider<C> createConnectionProvider(ExtensionModel extensionModel,
                                                     ConnectionProviderModel connectionProviderModel,
                                                     Map<String, Object> parameters,
                                                     String parametersOwner)
      throws ZernovaException;

  /**
   * Creates a {@link ResolverSetBasedParameterResolver} from a {@link ResolverSet} of a {@link ParameterizedModel} based on
   * static values of its parameters.
   *
   * @param actingParameter    the componentParameterization that describes the model parameter values.
   * @param parameterizedModel the owner of the parameters from the parameters resolver.
   * @return the corresponding {@link ParameterValueResolver}
   * @throws ZernovaException if the resolver cannot be created
   */
  ParameterValueResolver createParameterValueResolver(ComponentParameterization<?> actingParameter,
                                                      ParameterizedModel parameterizedModel)
      throws ZernovaException;

  ResolverSet createParametersResolverSetFromValues(Map<String, ?> values, ParameterizedModel parameterizedModel)
      throws ConfigurationException, InitialisationException;

  /**
   * Creates a new instance of a {@link ValueProviderMediator}.
   *
   * @param parameterizedModel container model which is a {@link ParameterizedModel} and {@link EnrichableModel}
   */
  ValueProviderMediator createValueProviderMediator(ParameterizedModel parameterizedModel);

  /**
   * Creates a new instance of a {@link SampleDataProviderMediator}.
   */
  SampleDataProviderMediator createSampleDataProviderMediator(ExtensionModel extensionModel,
                                                              ComponentModel componentModel,
                                                              Component component,
                                                              StreamingManager streamingManager);

  /**
   * Creates a new instance of a {@link MetadataContext}.
   */
  MetadataContext createMetadataContext(Supplier<Optional<ConfigurationInstance>> configurationSupplier,
                                        ConnectionManager connectionManager,
                                        MetadataCache cache,
                                        ClassTypeLoader typeLoader);

  /**
   * Creates a new instance of a {@link MetadataContext}.
   */
  MetadataContext createMetadataContext(Supplier<Optional<ConfigurationInstance>> configurationSupplier,
                                        ConnectionManager connectionManager, MetadataCache cache, ClassTypeLoader typeLoader,
                                        Optional<ScopeOutputMetadataContext> scopeOutputMetadataContext,
                                        Optional<RouterOutputMetadataContext> routerOutputMetadataContext);

  /**
   * Creates a new instance of a {@link MetadataMediator}.
   */
  <CM extends ComponentModel> MetadataMediator createMetadataMediator(CM componentModel);

}
