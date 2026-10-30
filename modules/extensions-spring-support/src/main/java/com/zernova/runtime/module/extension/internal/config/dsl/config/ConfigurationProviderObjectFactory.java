/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.config;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.util.func.CheckedConsumer;
import com.zernova.runtime.dsl.api.component.ObjectFactory;
import com.zernova.runtime.extension.api.property.ClassLoaderModelProperty;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.config.ConfigurationProviderFactory;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.exception.RequiredParameterNotSetException;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ImplicitConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticConnectionProviderResolver;

import java.util.Optional;

import jakarta.inject.Inject;

/**
 * A {@link AbstractExtensionObjectFactory} which produces {@link ConfigurationProvider} instances
 *
 * @since 4.0
 */
public class ConfigurationProviderObjectFactory extends AbstractExtensionObjectFactory<ConfigurationProvider>
    implements ObjectFactory<ConfigurationProvider> {

  private final ExtensionModel extensionModel;
  private final ConfigurationModel configurationModel;

  @Inject
  private ConfigurationProviderFactory configurationProviderFactory;

  private ExpirationPolicy expirationPolicy;
  private Optional<ConnectionProviderValueResolver> connectionProviderResolver = empty();
  private ConfigurationProvider instance;
  private boolean requiresConnection = false;
  private final LazyValue<String> configName = new LazyValue<>(this::getName);

  public ConfigurationProviderObjectFactory(ExtensionModel extensionModel,
                                            ConfigurationModel configurationModel,
                                            ZernovaContext muleContext) {
    super(muleContext);
    this.extensionModel = extensionModel;
    this.configurationModel = configurationModel;
  }

  @Override
  public ConfigurationProvider doGetObject() throws Exception {
    if (instance == null) {
      instance = createInnerInstance();
    }
    return instance;
  }

  private ConfigurationProvider createInnerInstance() throws ConfigurationException {
    // TODO: W-11365218
    if (expirationPolicy == null) {
      expirationPolicy = muleContext.getConfiguration().getDynamicConfigExpiration().getExpirationPolicy();
    }

    return withContextClassLoader(getExtensionClassLoader(), () -> {
      ResolverSet resolverSet = getParametersResolver().getParametersAsResolverSet(configurationModel, muleContext);
      final ConnectionProviderValueResolver connectionProviderResolver = getConnectionProviderResolver();
      connectionProviderResolver.getResolverSet()
          .ifPresent((CheckedConsumer) resolver -> initialiseIfNeeded(resolver, true, muleContext));

      ConfigurationProvider configurationProvider;
      try {
        if (resolverSet.isDynamic() || connectionProviderResolver.isDynamic()) {
          configurationProvider = configurationProviderFactory
              .createDynamicConfigurationProvider(configName.get(),
                                                  extensionModel,
                                                  configurationModel,
                                                  resolverSet,
                                                  connectionProviderResolver,
                                                  expirationPolicy);
        } else {
          configurationProvider = configurationProviderFactory
              .createStaticConfigurationProvider(configName.get(),
                                                 extensionModel,
                                                 configurationModel,
                                                 resolverSet,
                                                 connectionProviderResolver);
        }

      } catch (Exception e) {
        throw new ConfigurationException(createStaticMessage(format("Could not create an implicit configuration '%s' for the extension '%s': %s",
                                                                    configurationModel.getName(),
                                                                    extensionModel.getName(),
                                                                    e.getMessage())),
                                         e);
      }
      return configurationProvider;
    });
  }

  private ClassLoader getExtensionClassLoader() {
    return extensionModel.getModelProperty(ClassLoaderModelProperty.class).map(ClassLoaderModelProperty::getClassLoader)
        .orElse(currentThread().getContextClassLoader());
  }

  private ConnectionProviderValueResolver getConnectionProviderResolver() {
    return connectionProviderResolver.orElseGet(() -> {
      if (!requiresConnection) {
        return new StaticConnectionProviderResolver<>(null, null);
      } else {
        return new ImplicitConnectionProviderValueResolver(getName(), extensionModel, configurationModel, reflectionCache,
                                                           expressionManager, muleContext);
      }
    });
  }

  private String getName() {
    return configurationModel.getAllParameterModels().stream()
        .filter(ParameterModel::isComponentId)
        .findAny()
        .map(p -> ((ValueResolver) parameters.get(p.getName())))
        .map(vr -> {
          try {
            return ((String) vr.resolve(null));
          } catch (ZernovaException e) {
            throw new IllegalStateException("Error obtaining configuration name", e);
          }
        })
        .orElseThrow(() -> new RequiredParameterNotSetException("cannot create a configuration without a name"));
  }

  public void setExpirationPolicy(ExpirationPolicy expirationPolicy) {
    this.expirationPolicy = expirationPolicy;
  }

  public void setConnectionProviderResolver(ConnectionProviderResolver connectionProviderResolver) {
    this.connectionProviderResolver = ofNullable(connectionProviderResolver);
  }

  public void setRequiresConnection(boolean requiresConnection) {
    this.requiresConnection = requiresConnection;
  }
}
