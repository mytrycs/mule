/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.dsl.model.properties;

/**
 * Represents a configuration attribute.
 *
 * @since 4.1
 *
 * @deprecated since 4.4, use com.zernova.runtime.properties.api.ConfigurationProperty instead.
 */
@Deprecated
public interface ConfigurationProperty extends com.zernova.runtime.properties.api.ConfigurationProperty {

  /**
   * @return the plain configuration value without resolution. A configuration value may contain reference to other configuration
   *         attributes.
   */
  Object getRawValue();

  @Override
  default String getValue() {
    return getRawValue().toString();
  }
}
