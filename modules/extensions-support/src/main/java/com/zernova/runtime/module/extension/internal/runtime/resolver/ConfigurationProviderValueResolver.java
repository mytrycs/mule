/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.lang.String.format;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.isExpression;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;


/**
 * Implementation of {@link ValueResolver} which locates and returns a {@link ConfigurationProvider} associated with a given
 * configuration provider reference. The reference can either be the name of the configuration provider or an expression which
 * resolves to the name.
 * <p/>
 * The result of {@link #isDynamic()} will depend only on whether the given reference is an expression or not.
 *
 * @since 4.5.0
 */
public class ConfigurationProviderValueResolver implements ValueResolver<ConfigurationProvider>, Initialisable, ZernovaContextAware {

  private final ValueResolver<String> configurationProviderNameResolver;

  private ZernovaContext muleContext;
  private ConfigurationProvider cachedConfigurationProvider;

  /**
   * Construct a new instance.
   *
   * @param configurationProviderReference a not blank {@link String}
   */
  public ConfigurationProviderValueResolver(String configurationProviderReference) {
    if (isExpression(configurationProviderReference)) {
      configurationProviderNameResolver =
          new TypeSafeExpressionValueResolver<>(configurationProviderReference, String.class, STRING);
    } else {
      configurationProviderNameResolver = new StaticValueResolver<>(configurationProviderReference);
    }
  }

  /**
   * Locates and returns the {@link ConfigurationProvider} associated with the {@code configurationProviderReference} given upon
   * construction.
   *
   * @param context a {@link ValueResolvingContext}
   * @return the {@link ConfigurationProvider} associated with {@code configurationProviderReference}
   * @throws ZernovaException          if an error occurred fetching the value
   * @throws ConfigurationException if no object is registered under the name resolved from {@code configurationProviderReference}
   */
  @Override
  public ConfigurationProvider resolve(ValueResolvingContext context) throws ZernovaException {
    if (cachedConfigurationProvider != null) {
      return cachedConfigurationProvider;
    }

    ConfigurationProvider configurationProvider = doResolve(context);
    if (!isDynamic()) {
      cachedConfigurationProvider = configurationProvider;
    }
    return configurationProvider;
  }

  private ConfigurationProvider doResolve(ValueResolvingContext context) throws ZernovaException {
    String configurationProviderName = configurationProviderNameResolver.resolve(context);
    return muleContext.getExtensionManager().getConfigurationProvider(configurationProviderName)
        .orElseThrow(() -> new ConfigurationException(createStaticMessage(format("There is no registered configurationProvider under name '%s'",
                                                                                 configurationProviderName))));
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isDynamic() {
    return configurationProviderNameResolver.isDynamic();
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(configurationProviderNameResolver, true, muleContext);
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    muleContext = context;
  }
}
