/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context.lazy;

import static com.zernova.runtime.config.api.LazyComponentInitializer.LAZY_COMPONENT_INITIALIZER_SERVICE_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_MEMORY_MANAGEMENT_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONNECTIVITY_TESTER_FACTORY;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.util.ResourceLocator;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.internal.context.SpringZernovaContextServiceConfigurator;
import com.zernova.runtime.config.internal.lazy.NoOpConnectivityTesterFactory;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.el.function.ZernovaFunctionsBindingContextProvider;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Specialization of SpringZernovaContextServiceConfigurator that declares beans override for lazyInit.
 *
 * @since 4.5
 */
public class LazySpringZernovaContextServiceConfigurator extends SpringZernovaContextServiceConfigurator {

  private final LazyComponentInitializerAdapter lazyComponentInitializer;

  public LazySpringZernovaContextServiceConfigurator(LazyComponentInitializerAdapter lazyComponentInitializer,
                                                  Map<String, String> artifactProperties,
                                                  ZernovaContextWithRegistry muleContext,
                                                  ZernovaFunctionsBindingContextProvider coreFunctionsProvider,
                                                  ConfigurationProperties configurationProperties,
                                                  ArtifactType artifactType,
                                                  ArtifactAst artifactAst,
                                                  BeanDefinitionRegistry beanDefinitionRegistry,
                                                  Registry serviceLocator,
                                                  ResourceLocator resourceLocator,
                                                  MemoryManagementService memoryManagementService) {
    super(muleContext, coreFunctionsProvider, configurationProperties, artifactProperties,
          artifactType,
          artifactAst,
          beanDefinitionRegistry,
          serviceLocator, resourceLocator, memoryManagementService);
    this.lazyComponentInitializer = lazyComponentInitializer;
  }

  @Override
  protected void createArtifactServices() {
    super.createArtifactServices();

    registerBeanDefinition(OBJECT_CONNECTIVITY_TESTER_FACTORY, getBeanDefinition(NoOpConnectivityTesterFactory.class));
    registerConstantBeanDefinition(MULE_MEMORY_MANAGEMENT_SERVICE, getMemoryManagementService(), true);
    registerConstantBeanDefinition(LAZY_COMPONENT_INITIALIZER_SERVICE_KEY, lazyComponentInitializer, true);
  }

  protected ZernovaRegistry getRegistry() {
    return getMuleContext().getRegistry();
  }

}
