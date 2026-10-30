/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.SYSTEM_PROPERTY_PREFIX;
import static com.zernova.runtime.config.internal.model.ComponentBuildingDefinitionRegistryFactory.CACHING_FACTORY;
import static com.zernova.runtime.config.internal.model.ComponentBuildingDefinitionRegistryFactory.DEFAULT_FACTORY;

import static java.lang.Boolean.getBoolean;

import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import com.zernova.runtime.config.internal.ArtifactAstConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link ConfigurationBuilder} specialization that can provide a new {@link ArtifactContextFactory}.
 *
 * @since 4.4
 */
public interface ArtifactContextFactory extends ConfigurationBuilder {

  static final String CACHE_COMPONENT_BUILDING_DEFINITION_REGISTRY_PROPERTY =
      SYSTEM_PROPERTY_PREFIX + ".functionalTest.cacheComponentBuildingDefinitionRegistry";
  /**
   * Disable the caching of the BuildingDefinitionRegistry, regardless of the value of
   * {@code mule.functionalTest.cacheComponentBuildingDefinitionRegistry}.
   */
  static final String CACHE_COMPONENT_BUILDING_DEFINITION_REGISTRY_DISABLE_OVERRIDE_PROPERTY =
      SYSTEM_PROPERTY_PREFIX + ".functionalTest.cacheComponentBuildingDefinitionRegistry.disableOverride";

  /**
   * Creates a new {@link ArtifactContextFactory} based on the given ArtifactctAst and its creation parameters.
   *
   * @since 4.7
   */
  @Deprecated(since = "4.10", forRemoval = true)
  public static ArtifactContextFactory createArtifactContextFactory(ArtifactAst artifactAst,
                                                                    Map<String, String> artifactProperties,
                                                                    ArtifactType artifactType,
                                                                    boolean enableLazyInit,
                                                                    boolean addToolingObjectsToRegistry,
                                                                    List<ServiceConfigurator> serviceConfigurators,
                                                                    Optional<ArtifactContext> parentArtifactContext)
      throws ConfigurationException {
    return createArtifactContextFactory(artifactAst,
                                        artifactProperties,
                                        artifactType,
                                        enableLazyInit,
                                        serviceConfigurators,
                                        parentArtifactContext);
  }

  /**
   * Creates a new {@link ArtifactContextFactory} based on the given ArtifactctAst and its creation parameters.
   *
   * @since 4.10
   */
  public static ArtifactContextFactory createArtifactContextFactory(ArtifactAst artifactAst,
                                                                    Map<String, String> artifactProperties,
                                                                    ArtifactType artifactType,
                                                                    boolean enableLazyInit,
                                                                    List<ServiceConfigurator> serviceConfigurators,
                                                                    Optional<ArtifactContext> parentArtifactContext)
      throws ConfigurationException {
    ArtifactAstConfigurationBuilder configurationBuilder =
        new ArtifactAstConfigurationBuilder(artifactAst,
                                            artifactProperties,
                                            artifactType,
                                            enableLazyInit,
                                            createComponentBuildingDefinitionRegistry(artifactAst));

    parentArtifactContext
        .ifPresent(parentContext -> configurationBuilder.setParentContext(parentContext.getMuleContext(),
                                                                          parentContext.getArtifactAst()));
    serviceConfigurators.stream().forEach(configurationBuilder::addServiceConfigurator);

    return configurationBuilder;
  }

  public static ComponentBuildingDefinitionRegistry createComponentBuildingDefinitionRegistry(ArtifactAst artifactAst) {
    if (getBoolean(CACHE_COMPONENT_BUILDING_DEFINITION_REGISTRY_PROPERTY)
        && !getBoolean(CACHE_COMPONENT_BUILDING_DEFINITION_REGISTRY_DISABLE_OVERRIDE_PROPERTY)) {
      return CACHING_FACTORY.create(artifactAst.dependencies(), artifactAst::dependenciesDsl);
    } else {
      return DEFAULT_FACTORY.create(artifactAst.dependencies(), artifactAst::dependenciesDsl);
    }
  }

  /**
   *
   * @return a new {@link ArtifactContext} from the state of this implementation.
   */
  ArtifactContext createArtifactContext();

}
