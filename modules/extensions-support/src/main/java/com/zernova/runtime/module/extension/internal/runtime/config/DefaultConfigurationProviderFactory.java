/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONNECTION_MANAGER;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.getInitialiserEvent;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.withExtensionClassLoader;

import static java.lang.String.format;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.metadata.internal.ZernovaMetadataService;
import com.zernova.runtime.module.extension.api.runtime.config.ConfigurationProviderFactory;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import jakarta.inject.Inject;

/**
 * Default implementation of {@link ConfigurationProviderFactory}
 *
 * @since 4.0
 */
public final class DefaultConfigurationProviderFactory implements ConfigurationProviderFactory {

  @Inject
  private ReflectionCache reflectionCache;
  @Inject
  private ExpressionManager expressionManager;
  @Inject
  private ZernovaContext muleContext;

  /**
   * {@inheritDoc}
   */
  @Override
  public ConfigurationProvider createDynamicConfigurationProvider(String name,
                                                                  ExtensionModel extensionModel,
                                                                  ConfigurationModel configurationModel,
                                                                  ResolverSet resolverSet,
                                                                  ConnectionProviderValueResolver connectionProviderResolver,
                                                                  ExpirationPolicy expirationPolicy) {

    configureConnectionProviderResolver(name, connectionProviderResolver);
    return new DynamicConfigurationProvider(name, extensionModel, configurationModel, resolverSet, connectionProviderResolver,
                                            expirationPolicy, reflectionCache, expressionManager, muleContext);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConfigurationProvider createStaticConfigurationProvider(String name,
                                                                 ExtensionModel extensionModel,
                                                                 ConfigurationModel configurationModel,
                                                                 ResolverSet resolverSet,
                                                                 ConnectionProviderValueResolver connectionProviderResolver)
      throws Exception {
    return withExtensionClassLoader(extensionModel, () -> {
      configureConnectionProviderResolver(name, connectionProviderResolver);
      ConfigurationInstance configuration;
      CoreEvent initialiserEvent = null;
      try {
        initialiserEvent = getInitialiserEvent(muleContext);
        initialiseIfNeeded(resolverSet, true, muleContext);
        ConfigurationInstanceFactory configurationFactory = new ConfigurationInstanceFactory(extensionModel,
                                                                                             configurationModel,
                                                                                             resolverSet,
                                                                                             expressionManager,
                                                                                             muleContext);
        configuration = configurationFactory.createConfiguration(name, initialiserEvent, connectionProviderResolver);
      } catch (ZernovaException e) {
        throw new ConfigurationException(createStaticMessage(format("Could not create configuration '%s' for the '%s'", name,
                                                                    extensionModel.getName())),
                                         e);
      } finally {
        if (initialiserEvent != null) {
          ((BaseEventContext) initialiserEvent.getContext()).success();
        }
      }

      DefaultRegistry registry = new DefaultRegistry(muleContext);
      return registry.<ZernovaMetadataService>lookupByType(ZernovaMetadataService.class)
          .map(metadataService -> (StaticConfigurationProvider) new ConfigurationProviderToolingAdapter(name,
                                                                                                        extensionModel,
                                                                                                        configurationModel,
                                                                                                        configuration,
                                                                                                        metadataService,
                                                                                                        registry
                                                                                                            .<ConnectionManager>lookupByName(OBJECT_CONNECTION_MANAGER)
                                                                                                            .get(),
                                                                                                        reflectionCache,
                                                                                                        muleContext))
          .orElseGet(() -> new StaticConfigurationProvider(name, extensionModel,
                                                           configurationModel, configuration,
                                                           muleContext));
    });
  }

  private void configureConnectionProviderResolver(String configName, ValueResolver<ConnectionProvider> resolver) {
    if (resolver instanceof ConnectionProviderResolver) {
      ((ConnectionProviderResolver) resolver).setOwnerConfigName(configName);
    }
  }
}
