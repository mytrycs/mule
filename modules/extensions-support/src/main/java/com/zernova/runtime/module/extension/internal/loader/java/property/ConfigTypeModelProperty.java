/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;


import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ModelProperty;

import java.lang.ref.WeakReference;

/**
 * An immutable model property which specifies that the owning {@link EnrichableModel} requires a configuration of a given
 * {@link #configType}
 *
 * @since 4.0
 */
public class ConfigTypeModelProperty implements ModelProperty {

  private final WeakReference<Class<?>> configType;

  /**
   * Creates a new instance for the given {@code configType}
   *
   * @param configType
   */
  public ConfigTypeModelProperty(Class<?> configType) {
    this.configType = new WeakReference<>(configType);
  }

  /**
   * @return the {@link {@link #configType }}
   */
  public Class<?> getConfigType() {
    return configType.get();
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code configType}
   */
  @Override
  public String getName() {
    return "configType";
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
