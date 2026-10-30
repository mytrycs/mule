/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.ioc.ObjectProvider;
import com.zernova.runtime.api.ioc.ObjectProviderConfiguration;

/**
 * Immutable implementation of {@link ObjectProviderConfiguration}
 *
 * @since 4.0
 */
public class ImmutableObjectProviderConfiguration implements ObjectProviderConfiguration {

  private final ObjectProvider artifactObjectProvider;
  private final ConfigurationProperties configurationProperties;

  public ImmutableObjectProviderConfiguration(ConfigurationProperties configurationProperties,
                                              ObjectProvider artifactObjectProvider) {
    this.configurationProperties = configurationProperties;
    this.artifactObjectProvider = artifactObjectProvider;
  }

  @Override
  public ObjectProvider getArtifactObjectProvider() {
    return artifactObjectProvider;
  }

  @Override
  public ConfigurationProperties getConfigurationProperties() {
    return configurationProperties;
  }

}
