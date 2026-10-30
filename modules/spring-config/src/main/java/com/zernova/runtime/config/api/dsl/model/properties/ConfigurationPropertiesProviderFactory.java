/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.dsl.model.properties;

import static com.zernova.runtime.config.internal.dsl.model.properties.ConfigurationPropertiesProviderFactoryUtils.resolveConfigurationParameters;

import static java.util.ServiceLoader.load;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.config.api.dsl.model.ConfigurationParameters;
import com.zernova.runtime.config.internal.dsl.model.DefaultConfigurationParameters;
import com.zernova.runtime.properties.api.ConfigurationPropertiesProvider;
import com.zernova.runtime.properties.api.ResourceProvider;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.UnaryOperator;

/**
 * Builds the provider for a custom configuration properties element.
 *
 * @since 4.1
 *
 * @deprecated since 4.4, use com.zernova.runtime.properties.api.ConfigurationPropertiesProviderFactory instead.
 */
@Deprecated
public interface ConfigurationPropertiesProviderFactory
    extends com.zernova.runtime.properties.api.ConfigurationPropertiesProviderFactory {

  /**
   * @return the component identifier of the supported element.
   */
  @Override
  ComponentIdentifier getSupportedComponentIdentifier();

  /**
   * Builds a properties provider for each matching configuration element.
   *
   * @param parameters               the configuration parameters, after resolving property placeholders
   * @param externalResourceProvider the resource provider for locating files (such as .properties and .yaml)
   * @return the properties provider
   */
  default ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                         ResourceProvider externalResourceProvider) {
    return createProvider(parameters, uri -> externalResourceProvider.getResourceAsStream(uri));
  }

  /**
   * Builds a properties provider for the provided {@code providerElementDeclaration}.
   *
   * @param providerElementDeclaration the configuration parameters, after resolving property placeholders
   * @param localResolver              the resolver of property placeholders found in the provided declaration
   * @param externalResourceProvider   the resource provider for locating files (such as .properties and .yaml)
   * @return the properties provider
   */
  @Override
  default ConfigurationPropertiesProvider createProvider(ComponentAst providerElementDeclaration,
                                                         UnaryOperator<String> localResolver,
                                                         ResourceProvider externalResourceProvider) {
    DefaultConfigurationParameters.Builder configurationParametersBuilder = DefaultConfigurationParameters.builder();
    ConfigurationParameters configurationParameters =
        resolveConfigurationParameters(configurationParametersBuilder, providerElementDeclaration, localResolver);

    return createProvider(configurationParameters, uri -> externalResourceProvider.getResourceAsStream(uri));
  }

  /**
   * Builds a properties provider for each matching configuration element.
   *
   * @param parameters               the configuration parameters, after resolving property placeholders
   * @param externalResourceProvider the resource provider for locating files (such as .properties and .yaml)
   * @return the properties provider
   */
  com.zernova.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                                                                  com.zernova.runtime.config.api.dsl.model.ResourceProvider externalResourceProvider);

  /**
   * Support of the old deprecated interface. This may happen only on environments where the runtime modules are uses as libs in
   * some tool, but not when inside the Runtime.
   *
   * @param providerFactoriesMap where the found providers will be added;
   */
  public static void loadDeprecatedProviderFactories(Map<ComponentIdentifier, ConfigurationPropertiesProviderFactory> providerFactoriesMap) {
    ServiceLoader<? extends ConfigurationPropertiesProviderFactory> providerFactoriesOld =
        load(ConfigurationPropertiesProviderFactory.class);
    providerFactoriesOld.forEach(service -> {
      ComponentIdentifier componentIdentifier = service.getSupportedComponentIdentifier();
      if (providerFactoriesMap.containsKey(componentIdentifier)) {
        // skipping already present factory with the newer api
        return;
      }
      providerFactoriesMap.put(componentIdentifier, service);
    });

  }

}
