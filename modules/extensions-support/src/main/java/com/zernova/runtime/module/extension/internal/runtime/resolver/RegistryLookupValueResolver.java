/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.util.func.Once;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import jakarta.inject.Inject;

/**
 * Implementation of {@link ValueResolver} which accesses the mule registry and returns the value associated with {@link #key}.
 * <p/>
 * Although the registry is mutable, {@link #isDynamic()} will always return {@code false} since the value associated to a given
 * key is not meant to change.
 *
 * @since 3.7.0
 */
public class RegistryLookupValueResolver<T> implements ValueResolver<T> {

  private final String key;
  private final Once.RunOnce keyValidator;

  private Registry registry;

  /**
   * Construct a new instance and set the {@link #key} that will be used to access the registry
   *
   * @param key a not blank {@link String}
   */
  public RegistryLookupValueResolver(String key) {
    this.key = key;
    this.keyValidator = Once.of(() -> checkArgument(!isBlank(key), "A null or empty key was provided. "
        + "Registry lookup cannot be performed with a blank key"));
  }

  /**
   * Returns the registry value associated with {@link #key}
   *
   * @param context a {@link ValueResolvingContext}
   * @return the registry value associated with {@link #key}
   * @throws ZernovaException          if an error occurred fetching the value
   * @throws ConfigurationException if no object is registered under {@link #key}
   */
  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    keyValidator.runOnce();
    return registry.<T>lookupByName(key)
        .orElseThrow(() -> new ConfigurationException(createStaticMessage(format("Element '%s' is not defined in the Mule Registry",
                                                                                 key))));
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isDynamic() {
    return false;
  }

  @Inject
  public void setRegistry(Registry registry) {
    this.registry = registry;
  }
}
