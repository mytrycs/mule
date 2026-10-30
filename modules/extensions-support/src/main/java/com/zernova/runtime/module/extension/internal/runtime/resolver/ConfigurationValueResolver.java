/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

/**
 * A value resolver which returns the values of a {@link ConfigurationInstance} obtained through a {@link ConfigurationProvider}
 *
 * @param <T> the generic type of the resolved values
 * @since 4.1
 */
public class ConfigurationValueResolver<T> implements ValueResolver<T> {

  private final ConfigurationProvider configurationProvider;

  public ConfigurationValueResolver(ConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
  }

  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    ConfigurationInstance configurationInstance = configurationProvider.get(context.getEvent());
    return (T) configurationInstance.getValue();
  }

  @Override
  public boolean isDynamic() {
    return configurationProvider.isDynamic();
  }
}
