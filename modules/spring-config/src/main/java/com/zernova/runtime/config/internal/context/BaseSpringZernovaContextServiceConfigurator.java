/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import static com.zernova.runtime.api.config.FeatureFlaggingService.FEATURE_FLAGGING_SERVICE_KEY;
import static com.zernova.runtime.api.serialization.ObjectSerializer.DEFAULT_OBJECT_SERIALIZER_NAME;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ALERTING_SUPPORT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ARTIFACT_ENCODING;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONFIGURATION_PROPERTIES;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXPRESSION_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONFIGURATION;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_REGISTRY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SCHEDULER_BASE_CONFIG;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SCHEDULER_POOLS_CONFIG;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TIME_SUPPLIER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSFORMERS_REGISTRY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSFORMER_RESOLVER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static com.zernova.runtime.core.internal.config.bootstrap.AbstractRegistryBootstrap.BINDING_PROVIDER_PREDICATE;
import static com.zernova.runtime.core.internal.config.bootstrap.AbstractRegistryBootstrap.TRANSFORMER_PREDICATE;
import static com.zernova.runtime.core.internal.exception.ErrorTypeLocatorFactory.createDefaultErrorTypeLocator;

import static java.lang.Boolean.getBoolean;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Optional.of;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.scheduler.SchedulerContainerPoolsConfig;
import com.zernova.runtime.config.internal.bean.ZernovaConfigurationDelegate;
import com.zernova.runtime.config.internal.bean.DefaultObjectSerializerDelegate;
import com.zernova.runtime.config.internal.el.DataWeaveExtendedExpressionLanguageAdaptorFactoryBean;
import com.zernova.runtime.config.internal.el.DefaultExpressionManagerFactoryBean;
import com.zernova.runtime.config.internal.factories.SchedulerBaseConfigFactory;
import com.zernova.runtime.config.internal.lazy.LazyDataWeaveExtendedExpressionLanguageAdaptorFactoryBean;
import com.zernova.runtime.config.internal.registry.SpringRegistryBootstrap;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.internal.alert.DefaultAlertingSupport;
import com.zernova.runtime.core.internal.config.CustomService;
import com.zernova.runtime.core.internal.config.InternalCustomizationService;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeRepository;
import com.zernova.runtime.core.internal.registry.TypeBasedTransformerResolver;
import com.zernova.runtime.core.internal.time.LocalTimeSupplier;
import com.zernova.runtime.core.internal.transformer.DefaultTransformersRegistry;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import jakarta.inject.Inject;

/**
 * This class configures the basic services available in a {@code ZernovaContext} that are independent of the artifact config.
 * <p>
 * There's a predefined set of services plus a configurable set of services provided by
 * {@code ZernovaContext#getCustomizationService}.
 * <p>
 * This class takes cares of registering bean definitions for each of the provided services so dependency injection can be
 * properly done through the use of {@link Inject}.
 *
 * @since 4.5
 */
public class BaseSpringZernovaContextServiceConfigurator extends AbstractSpringZernovaContextServiceConfigurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseSpringZernovaContextServiceConfigurator.class);

  // This is needed just for some unit test scenarios
  // TODO MULE-20028 remove this
  @Deprecated
  public static final String DISABLE_TRANSFORMERS_SUPPORT =
      BaseSpringZernovaContextServiceConfigurator.class.getName() + ".disableTransformersSupport";

  // Do not use static field. BeanDefinitions are reused and produce weird behaviour
  private final Map<String, BeanDefinition> baseContextServices =
      ofEntries(entry(OBJECT_TIME_SUPPLIER, getBeanDefinition(LocalTimeSupplier.class)),
                entry(OBJECT_ALERTING_SUPPORT, getBeanDefinition(DefaultAlertingSupport.class)));

  private final ZernovaContext muleContext;
  private final ArtifactType artifactType;
  private final ConfigurationProperties configurationProperties;
  private final boolean enableLazyInit;
  private com.zernova.runtime.core.internal.registry.Registry originalRegistry;

  public BaseSpringZernovaContextServiceConfigurator(ZernovaContext muleContext,
                                                  ConfigurationProperties configurationProperties,
                                                  ArtifactType artifactType,
                                                  BeanDefinitionRegistry beanDefinitionRegistry,
                                                  Registry serviceLocator,
                                                  com.zernova.runtime.core.internal.registry.Registry originalRegistry,
                                                  boolean enableLazyInit) {
    super((InternalCustomizationService) muleContext.getCustomizationService(), beanDefinitionRegistry, serviceLocator);
    this.muleContext = muleContext;
    this.configurationProperties = configurationProperties;
    this.artifactType = artifactType;
    this.originalRegistry = originalRegistry;
    this.enableLazyInit = enableLazyInit;
  }

  void createArtifactServices() {
    FeatureFlaggingService featureFlaggingService = originalRegistry.lookupObject(FEATURE_FLAGGING_SERVICE_KEY);
    registerConstantBeanDefinition(FEATURE_FLAGGING_SERVICE_KEY, featureFlaggingService);
    registerConstantBeanDefinition(OBJECT_ARTIFACT_ENCODING, originalRegistry.lookupObject(OBJECT_ARTIFACT_ENCODING));

    registerConstantBeanDefinition(ConfigurationComponentLocator.REGISTRY_KEY, new BaseConfigurationComponentLocator());

    if (!artifactType.equals(DOMAIN)) {
      loadServiceConfigurators();
    }
    registerContextServices(baseContextServices, artifactType.equals(DOMAIN));

    // Instances of the repository and locator need to be injected into another objects before actually determining the possible
    // values. This contributing layer is needed to ensure the correct functioning of the DI mechanism while allowing actual
    // values to be provided at a later time.
    final ContributedErrorTypeRepository contributedErrorTypeRepository = new ContributedErrorTypeRepository();
    registerConstantBeanDefinition(ErrorTypeRepository.class.getName(), contributedErrorTypeRepository);
    final ContributedErrorTypeLocator contributedErrorTypeLocator = new ContributedErrorTypeLocator();
    contributedErrorTypeLocator
        .setDelegate(createDefaultErrorTypeLocator(contributedErrorTypeRepository, of(featureFlaggingService)));
    registerConstantBeanDefinition(ErrorTypeLocator.class.getName(), contributedErrorTypeLocator);

    registerConstantBeanDefinition(OBJECT_CONFIGURATION_PROPERTIES, configurationProperties);

    if (!getBoolean(DISABLE_TRANSFORMERS_SUPPORT)) {
      registerBeanDefinition(OBJECT_TRANSFORMER_RESOLVER, getBeanDefinition(TypeBasedTransformerResolver.class));
      registerBeanDefinition(OBJECT_TRANSFORMERS_REGISTRY, getBeanDefinition(DefaultTransformersRegistry.class));
    }

    registerLazyInitialisationAwareBeans();

    registerBeanDefinition(OBJECT_EXPRESSION_MANAGER, getBeanDefinition(DefaultExpressionManagerFactoryBean.class));

    registerBeanDefinition(OBJECT_SCHEDULER_POOLS_CONFIG,
                           getConstantObjectBeanDefinition(SchedulerContainerPoolsConfig.getInstance()));
    registerBeanDefinition(OBJECT_SCHEDULER_BASE_CONFIG, getBeanDefinition(SchedulerBaseConfigFactory.class));

    registerConstantBeanDefinition(OBJECT_REGISTRY, getServiceLocator());
    registerBeanDefinition(DEFAULT_OBJECT_SERIALIZER_NAME, getBeanDefinition(DefaultObjectSerializerDelegate.class));
    registerBeanDefinition(OBJECT_MULE_CONFIGURATION, getBeanDefinition(ZernovaConfigurationDelegate.class));

    createRuntimeServices();
    createBootstrapBeanDefinitions();
    absorbOriginalRegistry();
  }

  private void registerLazyInitialisationAwareBeans() {
    if (enableLazyInit) {
      registerBeanDefinition(OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER,
                             getBeanDefinition(LazyDataWeaveExtendedExpressionLanguageAdaptorFactoryBean.class));
    } else {
      registerBeanDefinition(OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER,
                             getBeanDefinition(DataWeaveExtendedExpressionLanguageAdaptorFactoryBean.class));
    }
  }

  protected void createBootstrapBeanDefinitions() {
    try {
      SpringRegistryBootstrap springRegistryBootstrap =
          new SpringRegistryBootstrap(artifactType,
                                      muleContext.getRegistryBootstrapServiceDiscoverer(),
                                      this::registerBeanDefinition,
                                      BINDING_PROVIDER_PREDICATE
                                          .or(TRANSFORMER_PREDICATE));
      springRegistryBootstrap.initialise();
    } catch (InitialisationException e) {
      throw new RuntimeException(e);
    }
  }

  private void createRuntimeServices() {
    final Map<String, CustomService> customServices = getCustomizationService().getCustomServices();
    for (String serviceName : customServices.keySet()) {

      if (containsBeanDefinition(serviceName)) {
        throw new IllegalStateException("There is already a bean definition registered with key: " + serviceName);
      }

      final CustomService customService = customServices.get(serviceName);
      if (customService.isBaseContext()
          // TODO MULE-19927 get these form a more specific place and avoid this filter
          || isServiceRuntimeProvided(customService)) {
        final BeanDefinition beanDefinition = getCustomServiceBeanDefinition(customService, serviceName);

        LOGGER.debug("Registering runtime service '{}' for {}...", serviceName, artifactType.name());
        registerBeanDefinition(serviceName, beanDefinition);
      }
    }
  }

  private void absorbOriginalRegistry() {
    if (originalRegistry == null) {
      return;
    }

    originalRegistry.lookupByType(Object.class)
        .forEach(this::registerConstantBeanDefinition);
    originalRegistry = null;
  }

}
