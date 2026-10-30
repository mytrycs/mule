/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.model.properties;

import com.zernova.runtime.config.api.dsl.model.properties.ConfigurationProperty;

/**
 * Represents a configuration attribute.
 * <p>
 * This exists to provide an adaptability layer between old properties support and the new mechanism introduced in 4.4.
 *
 * @since 4.5
 *
 * @deprecated Use {@link com.zernova.runtime.config.internal.model.dsl.config.DefaultConfigurationProperty} instead.
 */
@Deprecated
public class DefaultConfigurationProperty extends com.zernova.runtime.config.internal.model.dsl.config.DefaultConfigurationProperty
    implements ConfigurationProperty {

  public DefaultConfigurationProperty(Object source, String parentPath, String value) {
    super(source, parentPath, value);
  }

  @Override
  public Object getRawValue() {
    return super.getValue();
  }

}
