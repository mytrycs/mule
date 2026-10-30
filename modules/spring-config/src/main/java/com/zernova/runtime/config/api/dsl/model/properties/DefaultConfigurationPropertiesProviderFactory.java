/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.dsl.model.properties;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.util.Preconditions;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.config.api.dsl.model.ConfigurationParameters;
import com.zernova.runtime.properties.api.ConfigurationPropertiesProvider;
import com.zernova.runtime.properties.api.DefaultConfigurationPropertiesProvider;
import com.zernova.runtime.properties.api.ResourceProvider;

import java.util.function.UnaryOperator;

/**
 * Builds the provider for the configuration-properties element.
 *
 * @since 4.1
 * @deprecated since 4.4, use the SDK instead of registering parsers manually.
 */
@Deprecated
public final class DefaultConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

  public static final String CONFIGURATION_PROPERTIES_ELEMENT = "configuration-properties";
  public static final ComponentIdentifier CONFIGURATION_PROPERTIES =
      builder().namespace(CORE_PREFIX).name(CONFIGURATION_PROPERTIES_ELEMENT).build();

  @Override
  public ComponentIdentifier getSupportedComponentIdentifier() {
    return CONFIGURATION_PROPERTIES;
  }

  @Override
  public ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                        ResourceProvider externalResourceProvider) {

    String file = parameters.getStringParameter("file");
    Preconditions.checkArgument(file != null, "Required attribute 'file' of 'configuration-properties' not found");
    String encoding = parameters.getStringParameter("encoding");
    return new DefaultConfigurationPropertiesProvider(file, encoding, externalResourceProvider);
  }

  @Override
  public ConfigurationPropertiesProvider createProvider(ComponentAst providerElementDeclaration,
                                                        UnaryOperator<String> localResolver,
                                                        ResourceProvider externalResourceProvider) {
    return createProvider(providerElementDeclaration, localResolver, uri -> externalResourceProvider.getResourceAsStream(uri));
  }

  @Override
  public com.zernova.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                                                                         com.zernova.runtime.config.api.dsl.model.ResourceProvider externalResourceProvider) {
    return createProvider(parameters, uri -> externalResourceProvider.getResourceAsStream(uri));
  }
}
