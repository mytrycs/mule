/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getAnnotatedFieldsStream;

import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.lifecycle.InjectedDependenciesProvider;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import java.lang.reflect.Field;
import java.util.Collection;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * {@link ConfigurationProvider} which provides always the same {@link #configuration}.
 *
 * @since 3.7.0
 */
public class StaticConfigurationProvider extends LifecycleAwareConfigurationProvider implements InjectedDependenciesProvider {

  private final ConfigurationInstance configuration;

  public StaticConfigurationProvider(String name, ExtensionModel extensionModel, ConfigurationModel configurationModel,
                                     ConfigurationInstance configuration, ZernovaContext muleContext) {
    super(name, extensionModel, configurationModel, muleContext);
    this.configuration = configuration;
    registerConfiguration(configuration);
  }

  /**
   * Returns {@link #configuration}.
   *
   * @param muleEvent the current {@link CoreEvent}
   * @return {@link #configuration}
   */
  @Override
  public ConfigurationInstance get(Event muleEvent) {
    return configuration;
  }

  @Override
  public Collection<Either<Class<?>, String>> getInjectedDependencies() {
    return getAnnotatedFieldsStream(configuration.getValue().getClass(), Inject.class,
                                    // Still need to support javax.inject for the time being...
                                    javax.inject.Inject.class)
                                        .map(field -> {
                                          Named name = field.getAnnotation(Named.class);
                                          return name != null
                                              ? Either.<Class<?>, String>right(name.value())
                                              : javaxNamed(field);
                                        }).toList();
  }

  // Still need to support javax.inject for the time being...
  private Either<Class<?>, String> javaxNamed(Field field) {
    javax.inject.Named name = field.getAnnotation(javax.inject.Named.class);
    return name != null
        ? Either.<Class<?>, String>right(name.value())
        : Either.<Class<?>, String>left(field.getType());
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isDynamic() {
    return false;
  }
}
