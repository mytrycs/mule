/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.bootstrap;

import static java.util.Collections.singletonList;

import com.zernova.runtime.core.api.config.bootstrap.BootstrapService;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.config.bootstrap.PropertiesBootstrapService;

import java.util.List;
import java.util.Properties;

/**
 * Defines a BootstrapServiceDiscoverer useful for testing purposes
 */
public class TestBootstrapServiceDiscoverer implements BootstrapServiceDiscoverer {

  private final Properties properties;

  /**
   * Creates a new instance that will discover only a {@link BootstrapService}
   *
   * @param properties properties that will be returned by the discovered {@link BootstrapService}
   */
  public TestBootstrapServiceDiscoverer(Properties properties) {
    this.properties = properties;
  }

  @Override
  public List<BootstrapService> discover() {
    return singletonList(new PropertiesBootstrapService(this.getClass().getClassLoader(), properties));
  }
}
