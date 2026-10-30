/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.model.dsl.properties;

import com.zernova.runtime.properties.api.DefaultConfigurationPropertiesProvider;
import com.zernova.runtime.properties.api.DefaultConfigurationPropertiesProviderFactory;
import com.zernova.runtime.properties.api.InitialisableConfigurationPropertiesProvider;
import com.zernova.runtime.properties.api.ResourceProvider;

/**
 * Builds the provider for DefaultInitialisableConfigurationPropertiesProvider.
 *
 */
public final class DefaultInitialisableConfigurationPropertiesProviderFactory
    implements DefaultConfigurationPropertiesProviderFactory {

  @Override
  public InitialisableConfigurationPropertiesProvider createProvider(String fileLocation, String encoding,
                                                                     ResourceProvider resourceProvider,
                                                                     DefaultConfigurationPropertiesProvider defaultConfigurationPropertiesProvider) {
    return new DefaultInitialisableConfigurationPropertiesProvider(fileLocation, encoding, resourceProvider,
                                                                   defaultConfigurationPropertiesProvider);
  }
}
