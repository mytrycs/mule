/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.model.properties;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.HONOUR_RESERVED_PROPERTIES;
import static com.zernova.runtime.config.api.properties.PropertiesResolverUtils.createGlobalPropertiesSupplier;
import static com.zernova.runtime.config.api.properties.PropertiesResolverUtils.getConfigurationPropertiesProvidersFromComponents;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesHierarchyBuilder;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesResolver;
import com.zernova.runtime.config.internal.model.dsl.config.PropertiesResolverConfigurationProperties;
import com.zernova.runtime.properties.api.ConfigurationProperty;
import com.zernova.runtime.properties.api.ResourceProvider;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides a common set of utilities for handling property resolvers for Mule artifacts.
 */
public class PropertiesHierarchyCreationUtils {

  private PropertiesHierarchyCreationUtils() {
    // Nothing to do
  }

  public static PropertiesResolverConfigurationProperties createConfigurationAttributeResolver(ArtifactAst artifactAst,
                                                                                               Optional<ConfigurationProperties> parentConfigurationProperties,
                                                                                               Map<String, String> deploymentProperties,
                                                                                               ResourceProvider externalResourceProvider,
                                                                                               Optional<FeatureFlaggingService> featureFlaggingService) {

    ConfigurationPropertiesHierarchyBuilder partialResolverBuilder = new ConfigurationPropertiesHierarchyBuilder();

    // MULE-17659: it should behave without the fix for applications made for runtime prior 4.2.2


    Supplier<Map<String, ConfigurationProperty>> globalPropertiesSupplier = createGlobalPropertiesSupplier(artifactAst);

    partialResolverBuilder
        .withDeploymentProperties(deploymentProperties)
        .withSystemProperties()
        .withEnvironmentProperties()
        .withGlobalPropertiesSupplier(globalPropertiesSupplier);

    ConfigurationPropertiesResolver partialResolver;
    if (featureFlaggingService.orElse(f -> true).isEnabled(HONOUR_RESERVED_PROPERTIES)) {
      partialResolver = partialResolverBuilder.build();
    } else {
      partialResolver = partialResolverBuilder.buildLegacyHierarchy();
    }

    artifactAst.updatePropertiesResolver(partialResolver);

    ConfigurationPropertiesHierarchyBuilder completeBuilder = new ConfigurationPropertiesHierarchyBuilder()
        .withDeploymentProperties(deploymentProperties)
        .withSystemProperties()
        .withEnvironmentProperties()
        .withPropertiesFile(externalResourceProvider)
        .withGlobalPropertiesSupplier(globalPropertiesSupplier);


    // Some configuration properties providers may depend their parameters on other properties, so we use the
    // partial resolution to create these resolvers, and then complete the entire hierarchy
    getConfigurationPropertiesProvidersFromComponents(artifactAst, externalResourceProvider, partialResolver)
        .forEach(completeBuilder::withApplicationProperties);

    parentConfigurationProperties.ifPresent(completeBuilder::withDomainPropertiesResolver);

    return new PropertiesResolverConfigurationProperties(completeBuilder.build());

  }

  public static PropertiesResolverConfigurationProperties createConfigurationAttributeResolver(Optional<ConfigurationProperties> parentConfigurationProperties,
                                                                                               Map<String, String> deploymentProperties,
                                                                                               ResourceProvider externalResourceProvider) {
    ConfigurationPropertiesHierarchyBuilder builder = new ConfigurationPropertiesHierarchyBuilder()
        .withDeploymentProperties(deploymentProperties)
        .withSystemProperties()
        .withEnvironmentProperties()
        .withPropertiesFile(externalResourceProvider);

    parentConfigurationProperties.ifPresent(builder::withDomainPropertiesResolver);

    return new PropertiesResolverConfigurationProperties(builder.build());
  }

}
