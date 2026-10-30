/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.bootstrap;

import com.zernova.runtime.core.api.config.bootstrap.RegistryBootstrapDiscoverer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Allows to configure a SimpleRegistryBootstrap using a single Properties object.
 */
public class SinglePropertiesRegistryBootstrapDiscoverer implements RegistryBootstrapDiscoverer {

  private final Properties properties;

  public SinglePropertiesRegistryBootstrapDiscoverer(Properties properties) {
    this.properties = properties;
  }

  @Override
  public List<Properties> discover() {
    return Arrays.asList(properties);
  }
}
