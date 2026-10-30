/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.api.metadata.resolving.MetadataFailure.Builder.newFailure;
import static com.zernova.runtime.api.metadata.resolving.MetadataResult.failure;
import static com.zernova.runtime.api.metadata.resolving.MetadataResult.success;
import static com.zernova.runtime.core.api.connection.util.ConnectionProviderUtils.unwrapProviderWrapper;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.extension.api.metadata.NullMetadataResolver.NULL_CATEGORY_NAME;
import static com.zernova.runtime.extension.api.values.ValueResolvingException.UNKNOWN;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.getInitialiserEvent;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getConnectionProviderModel;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getAllConnectionProviders;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getMetadataResolverFactory;
import static com.zernova.runtime.module.extension.internal.value.ValueProviderUtils.getValueProviderModels;
import static com.zernova.runtime.module.extension.internal.value.ValueProviderUtils.valuesWithClassLoader;

import static java.util.Optional.of;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.parameter.ValueProviderModel;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeyProvider;
import com.zernova.runtime.api.metadata.MetadataKeysContainer;
import com.zernova.runtime.api.metadata.MetadataKeysContainerBuilder;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.api.value.Value;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.values.ConfigurationParameterValueProvider;
import com.zernova.runtime.extension.api.values.ValueResolvingException;
import com.zernova.runtime.metadata.internal.ZernovaMetadataService;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.internal.metadata.DefaultMetadataContext;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ObjectBasedParameterValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.module.extension.internal.value.DefaultValueProviderMediator;

import java.util.List;
import java.util.Set;

/**
 * Adds the capability to expose tooling focused capabilities associated with the {@link StaticConfigurationProvider}'s
 * components. So far the capabilities are:
 * <ul>
 * <li>{@link MetadataKeyProvider}, to resolve {@link MetadataKey metadata keys} associated to a configuration</li>
 * <li>{@link ConfigurationParameterValueProvider}, to resolve {@link Value values} associated to a configuration and their
 * related connection</li>
 * </ul>
 *
 * @since 4.0
 */
public final class ConfigurationProviderToolingAdapter extends StaticConfigurationProvider
    implements MetadataKeyProvider, ConfigurationParameterValueProvider {

  private final ZernovaMetadataService metadataService;
  private final ConnectionManager connectionManager;
  private final ConfigurationInstance configuration;
  private final ReflectionCache reflectionCache;

  ConfigurationProviderToolingAdapter(String name,
                                      ExtensionModel extensionModel,
                                      ConfigurationModel configurationModel,
                                      ConfigurationInstance configuration,
                                      ZernovaMetadataService metadataService,
                                      ConnectionManager connectionManager,
                                      ReflectionCache reflectionCache,
                                      ZernovaContext muleContext) {
    super(name, extensionModel, configurationModel, configuration, muleContext);
    this.configuration = configuration;
    this.reflectionCache = reflectionCache;

    this.connectionManager = connectionManager;
    this.metadataService = metadataService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MetadataResult<MetadataKeysContainer> getMetadataKeys() {
    MetadataKeysContainerBuilder keysBuilder = MetadataKeysContainerBuilder.getInstance();
    ClassLoader classLoader = getClassLoader(getExtensionModel());
    return withContextClassLoader(classLoader, () -> {
      MetadataContext metadataContext = null;
      try {
        metadataContext = getMetadataContext(classLoader);
        addComponentKeys(getConfigurationModel().getOperationModels(), metadataContext, keysBuilder);
        addComponentKeys(getConfigurationModel().getSourceModels(), metadataContext, keysBuilder);
        return success(keysBuilder.build());
      } catch (Exception e) {
        return failure(newFailure(e).onKeys());
      } finally {
        if (metadataContext != null) {
          metadataContext.dispose();
        }
      }
    });
  }

  /**
   * Implementation for a configuration provider won't take into account the partialKey as it is defined only at the
   * operation/source level the key.
   *
   * {@inheritDoc}
   */
  @Override
  public MetadataResult<MetadataKeysContainer> getMetadataKeys(MetadataKey partialKey) throws MetadataResolvingException {
    return getMetadataKeys();
  }

  private void addComponentKeys(List<? extends ComponentModel> components, MetadataContext metadataContext,
                                MetadataKeysContainerBuilder keysBuilder)
      throws MetadataResolvingException, ConnectionException {
    for (ComponentModel component : components) {
      TypeKeysResolver keysResolver = getMetadataResolverFactory(component).getKeyResolver();

      String categoryName = keysResolver.getCategoryName();
      if (!NULL_CATEGORY_NAME.equals(categoryName) && !keysBuilder.containsCategory(categoryName)) {
        keysBuilder.add(categoryName, keysResolver.getKeys(metadataContext));
      }
    }
  }

  private MetadataContext getMetadataContext(ClassLoader classLoader) {
    return new DefaultMetadataContext(() -> {
      CoreEvent fakeEvent = null;
      try {
        fakeEvent = getInitialiserEvent(muleContext);
        return of(get(fakeEvent));
      } finally {
        if (fakeEvent != null) {
          ((BaseEventContext) fakeEvent.getContext()).success();
        }
      }
    }, connectionManager, metadataService.getMetadataCache(getName()), ExtensionsTypeLoaderFactory.getDefault()
        .createTypeLoader(classLoader));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Value> getConfigValues(String parameterName) throws ValueResolvingException {
    return valuesWithClassLoader(() -> {
      ConfigurationModel configurationModel = getConfigurationModel();
      return new DefaultValueProviderMediator<>(configurationModel, () -> muleContext, () -> reflectionCache)
          .getValues(parameterName, getParameterValueResolver(configuration.getValue(), configurationModel));
    }, getExtensionModel());
  }

  @Override
  public Set<Value> getConfigValues(String parameterName, String targetSelector) throws ValueResolvingException {
    return valuesWithClassLoader(() -> {
      ConfigurationModel configurationModel = getConfigurationModel();
      return new DefaultValueProviderMediator<>(configurationModel, () -> muleContext, () -> reflectionCache)
          .getValues(parameterName, targetSelector, getParameterValueResolver(configuration.getValue(), configurationModel));
    }, getExtensionModel());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ValueProviderModel> getConfigModels(String providerName) throws ValueResolvingException {
    return getValueProviderModels(getConfigurationModel().getAllParameterModels());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Value> getConnectionValues(String parameterName) throws ValueResolvingException {
    return valuesWithClassLoader(() -> withConnectionProviderInfo((connection, model) -> {
      DefaultValueProviderMediator<ConnectionProviderModel> valueProviderMediator =
          new DefaultValueProviderMediator<>(model, () -> muleContext, () -> reflectionCache);
      return valueProviderMediator.getValues(parameterName, getParameterValueResolver(connection, model));
    }), getExtensionModel());
  }

  @Override
  public Set<Value> getConnectionValues(String parameterName, String targetSelector) throws ValueResolvingException {
    return valuesWithClassLoader(() -> withConnectionProviderInfo((connection, model) -> {
      DefaultValueProviderMediator<ConnectionProviderModel> valueProviderMediator =
          new DefaultValueProviderMediator<>(model, () -> muleContext, () -> reflectionCache);
      return valueProviderMediator.getValues(parameterName, targetSelector, getParameterValueResolver(connection, model));
    }), getExtensionModel());
  }

  @Override
  public List<ValueProviderModel> getConnectionModels(String providerName) throws ValueResolvingException {
    return withConnectionProviderInfo((connection, model) -> getValueProviderModels(model.getAllParameterModels()));
  }

  private <T> T withConnectionProviderInfo(WithConnectionProviderCallable<T> withConnectionProviderCallable)
      throws ValueResolvingException {
    ConnectionProvider<?> connectionProvider = configuration.getConnectionProvider()
        .orElseThrow(() -> new ValueResolvingException("Unable to obtain the Connection Provider Instance", UNKNOWN));

    ConnectionProvider unwrap = unwrapProviderWrapper(connectionProvider);
    ConnectionProviderModel connectionProviderModel =
        getConnectionProviderModel(unwrap.getClass(), getAllConnectionProviders(getExtensionModel(), getConfigurationModel()))
            .orElseThrow(() -> new ValueResolvingException("Internal error. Unable to obtain the Connection Provider Model",
                                                           UNKNOWN));

    return withConnectionProviderCallable.call(unwrap, connectionProviderModel);
  }

  @FunctionalInterface
  private interface WithConnectionProviderCallable<T> {

    T call(ConnectionProvider connectionProvider, ConnectionProviderModel connectionProviderModel)
        throws ValueResolvingException;

  }

  private ParameterValueResolver getParameterValueResolver(Object object, ParameterizedModel configurationModel) {
    return new ObjectBasedParameterValueResolver(object, configurationModel, reflectionCache);
  }
}
