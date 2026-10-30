/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ParametersResolver.fromValues;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverSetUtils.getResolverSetFromComponentParameterization;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.metadata.MetadataCache;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.RouterOutputMetadataContext;
import com.zernova.runtime.api.metadata.ScopeOutputMetadataContext;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.config.ConfigurationProviderFactory;
import com.zernova.runtime.module.extension.api.runtime.config.ExtensionDesignTimeResolversFactory;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.api.tooling.metadata.MetadataMediator;
import com.zernova.runtime.module.extension.api.tooling.sampledata.SampleDataProviderMediator;
import com.zernova.runtime.module.extension.api.tooling.valueprovider.ValueProviderMediator;
import com.zernova.runtime.module.extension.internal.data.sample.DefaultSampleDataProviderMediator;
import com.zernova.runtime.module.extension.internal.metadata.DefaultMetadataContext;
import com.zernova.runtime.module.extension.internal.metadata.DefaultMetadataMediator;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ConnectionProviderSettings;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode.AuthorizationCodeOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials.ClientCredentialsOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs.PlatformManagedOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ParametersResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypeSafeValueResolverWrapper;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.module.extension.internal.value.DefaultValueProviderMediator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Inject;


public class DefaultExtensionDesignTimeResolversFactory implements ExtensionDesignTimeResolversFactory {

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private ReflectionCache reflectionCache;

  @Inject
  private ArtifactEncoding artifactEncoding;

  @Inject
  private ExpressionManager expressionManager;

  @Inject
  private Registry registry;

  @Override
  public <C> ConnectionProviderValueResolver<C> createConnectionProviderResolver(ConnectionProviderModel connectionProviderModel,
                                                                                 ComponentParameterization componentParameterization,
                                                                                 PoolingProfile poolingProfile,
                                                                                 ReconnectionConfig reconnectionConfig,
                                                                                 ExtensionModel extensionModel,
                                                                                 ConfigurationProperties configurationProperties,
                                                                                 String parametersOwner,
                                                                                 DslSyntaxResolver dslSyntaxResolver)
      throws ZernovaException {
    ConnectionProviderSettings settings = new ConnectionProviderSettings(connectionProviderModel,
                                                                         componentParameterization,
                                                                         poolingProfile,
                                                                         reconnectionConfig,
                                                                         // these are looked-up lazily
                                                                         lookup(AuthorizationCodeOAuthHandler.class),
                                                                         lookup(ClientCredentialsOAuthHandler.class),
                                                                         lookup(PlatformManagedOAuthHandler.class));

    return ConfigurationCreationUtils.createConnectionProviderResolver(extensionModel,
                                                                       settings,
                                                                       configurationProperties,
                                                                       expressionManager,
                                                                       reflectionCache,
                                                                       parametersOwner,
                                                                       dslSyntaxResolver,
                                                                       muleContext, artifactEncoding);
  }

  private <T> T lookup(Class<T> clazz) {
    return registry.lookupByType(clazz).orElse(null);
  }

  @Override
  public ConfigurationProvider createConfigurationProvider(ExtensionModel extensionModel,
                                                           ConfigurationModel configurationModel,
                                                           String configName,
                                                           Map<String, Object> parameters,
                                                           Optional<ExpirationPolicy> expirationPolicy,
                                                           Optional<ConnectionProviderValueResolver> connectionProviderResolver,
                                                           ConfigurationProviderFactory configurationProviderFactory,
                                                           String parametersOwner,
                                                           DslSyntaxResolver dslSyntaxResolver,
                                                           ClassLoader extensionClassLoader) {
    return ConfigurationCreationUtils.createConfigurationProvider(extensionModel,
                                                                  configurationModel,
                                                                  configName,
                                                                  parameters,
                                                                  expirationPolicy,
                                                                  connectionProviderResolver,
                                                                  configurationProviderFactory,
                                                                  expressionManager,
                                                                  reflectionCache,
                                                                  parametersOwner,
                                                                  dslSyntaxResolver,
                                                                  extensionClassLoader,
                                                                  muleContext, artifactEncoding);
  }

  @Override
  public <C> ConnectionProvider<C> createConnectionProvider(ExtensionModel extensionModel,
                                                            ConnectionProviderModel connectionProviderModel,
                                                            Map<String, Object> parameters,
                                                            String parametersOwner)
      throws ZernovaException {

    // TODO W-10992158
    ResolverSet resolverSet = createParametersResolverSetFromValues(parameters, connectionProviderModel);
    resolverSet.initialise();

    ConnectionProviderResolver<C> connectionProviderResolver =
        new ConnectionProviderResolver<>(new DefaultConnectionProviderObjectBuilder<>(connectionProviderModel,
                                                                                      resolverSet,
                                                                                      extensionModel,
                                                                                      expressionManager,
                                                                                      muleContext),
                                         resolverSet, muleContext);
    connectionProviderResolver.setOwnerConfigName(parametersOwner);

    ConnectionProvider<C> connectionProvider = connectionProviderResolver
        .getObjectBuilder().get()
        .build(resolveResolverSet(muleContext, resolverSet))
        .getFirst();

    return connectionProvider;
  }

  private ResolverSetResult resolveResolverSet(final ZernovaContext muleContext, ResolverSet resolverSet) throws ZernovaException {
    ResolverSetResult result;
    CoreEvent initializerEvent = CoreEvent.nullEvent();
    try {
      result = resolverSet.resolve(ValueResolvingContext.builder(initializerEvent).build());
    } finally {
      ((BaseEventContext) initializerEvent.getContext()).success();
    }
    return result;
  }


  @Override
  public ParameterValueResolver createParameterValueResolver(ComponentParameterization<?> actingParameter,
                                                             ParameterizedModel parameterizedModel)
      throws ZernovaException {
    ResolverSet resolverSet = getResolverSetFromComponentParameterization(actingParameter,
                                                                          muleContext,
                                                                          true,
                                                                          reflectionCache,
                                                                          expressionManager,
                                                                          parameterizedModel.getName(), artifactEncoding);
    return new DesignTimeParameterValueResolver(resolverSet,
                                                parameterizedModel,
                                                reflectionCache,
                                                expressionManager);
  }

  @Override
  public ResolverSet createParametersResolverSetFromValues(Map<String, ?> values, ParameterizedModel parameterizedModel)
      throws ConfigurationException, InitialisationException {
    final ParametersResolver parametersResolver = fromValues(values,
                                                             muleContext,
                                                             true,
                                                             reflectionCache,
                                                             expressionManager,
                                                             parameterizedModel.getName());

    ResolverSet typeUnsafeResolverSet = parametersResolver.getParametersAsResolverSet(muleContext,
                                                                                      parameterizedModel,
                                                                                      parameterizedModel
                                                                                          .getParameterGroupModels());

    Map<String, ParameterModel> paramModels =
        parameterizedModel.getAllParameterModels().stream()
            .collect(toMap(ParameterModel::getName, identity()));

    ResolverSet typeSafeResolverSet = new ResolverSet(muleContext);
    typeUnsafeResolverSet.getResolvers().forEach((paramName, resolver) -> {
      ParameterModel model = paramModels.get(paramName);
      if (model != null) {
        Optional<Class<Object>> clazz = getType(model.getType());
        if (clazz.isPresent()) {
          resolver = new TypeSafeValueResolverWrapper(resolver, clazz.get());
        }
      }

      typeSafeResolverSet.add(paramName, resolver);
    });

    typeSafeResolverSet.initialise();
    return typeSafeResolverSet;
  }

  @Override
  public ValueProviderMediator createValueProviderMediator(ParameterizedModel parameterizedModel) {
    return new DefaultValueProviderMediator(parameterizedModel,
                                            () -> muleContext,
                                            () -> reflectionCache);
  }

  @Override
  public SampleDataProviderMediator createSampleDataProviderMediator(ExtensionModel extensionModel,
                                                                     ComponentModel componentModel,
                                                                     Component component,
                                                                     StreamingManager streamingManager) {
    return new DefaultSampleDataProviderMediator(extensionModel,
                                                 componentModel,
                                                 component,
                                                 artifactEncoding,
                                                 muleContext.getNotificationManager(),
                                                 reflectionCache,
                                                 expressionManager,
                                                 streamingManager,
                                                 muleContext.getInjector(),
                                                 muleContext);
  }

  @Override
  public MetadataContext createMetadataContext(Supplier<Optional<ConfigurationInstance>> configurationSupplier,
                                               ConnectionManager connectionManager,
                                               MetadataCache cache,
                                               ClassTypeLoader typeLoader) {
    return new DefaultMetadataContext(configurationSupplier, connectionManager, cache, typeLoader);
  }

  @Override
  public MetadataContext createMetadataContext(Supplier<Optional<ConfigurationInstance>> configurationSupplier,
                                               ConnectionManager connectionManager, MetadataCache cache,
                                               ClassTypeLoader typeLoader,
                                               Optional<ScopeOutputMetadataContext> scopeOutputMetadataContext,
                                               Optional<RouterOutputMetadataContext> routerOutputMetadataContext) {
    return new DefaultMetadataContext(configurationSupplier, connectionManager, cache, typeLoader, scopeOutputMetadataContext,
                                      routerOutputMetadataContext);
  }

  @Override
  public <CM extends ComponentModel> MetadataMediator createMetadataMediator(CM componentModel) {
    return new DefaultMetadataMediator<>(componentModel,
                                         reflectionCache);
  }

}
