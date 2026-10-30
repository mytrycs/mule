/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;
import static com.zernova.runtime.config.internal.registry.AbstractSpringRegistry.SPRING_APPLICATION_CONTEXT;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.api.ArtifactContextFactory;
import com.zernova.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import com.zernova.runtime.config.internal.artifact.SpringArtifactContext;
import com.zernova.runtime.config.internal.context.BaseConfigurationComponentLocator;
import com.zernova.runtime.config.internal.context.BaseZernovaArtifactContext;
import com.zernova.runtime.config.internal.context.ZernovaArtifactContext;
import com.zernova.runtime.config.internal.context.lazy.LazyZernovaArtifactContext;
import com.zernova.runtime.config.internal.model.ComponentModelInitializer;
import com.zernova.runtime.config.internal.model.DefaultComponentBuildingDefinitionRegistryFactory;
import com.zernova.runtime.config.internal.registry.BaseSpringRegistry;
import com.zernova.runtime.config.internal.registry.SpringRegistry;
import com.zernova.runtime.config.internal.resolvers.ConfigurationDependencyResolver;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.lifecycle.LifecycleManager;
import com.zernova.runtime.core.internal.config.ParentZernovaContextAwareConfigurationBuilder;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.context.NullDomainZernovaContextLifecycleStrategy;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeRepository;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.internal.memory.management.ArtifactMemoryManagementService;
import com.zernova.runtime.internal.memory.management.DefaultMemoryManagementService;

import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Generates a Spring context for the artifact of the provided {@link ArtifactAst}, and sets it as the registry in the
 * {@link ZernovaContext}.
 *
 * @since 4.5
 */
public class ArtifactAstConfigurationBuilder extends AbstractConfigurationBuilder
    implements ParentZernovaContextAwareConfigurationBuilder, ArtifactContextFactory {

  private final boolean enableLazyInit;

  private final ArtifactAst artifactAst;
  private final Map<String, String> artifactProperties;
  private ApplicationContext parentContext;
  private ZernovaArtifactContext muleArtifactContext;
  private final ArtifactType artifactType;
  private final MemoryManagementService memoryManagementService;
  private final ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry;

  private ArtifactAstConfigurationBuilder(ArtifactAst artifactAst, Map<String, String> artifactProperties,
                                          ArtifactType artifactType, boolean enableLazyInit,
                                          MemoryManagementService memoryManagementService,
                                          ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry)
      throws ConfigurationException {
    this.artifactAst = artifactAst;
    this.artifactProperties = artifactProperties;
    this.artifactType = artifactType;
    this.enableLazyInit = enableLazyInit;
    this.memoryManagementService = memoryManagementService;
    this.componentBuildingDefinitionRegistry = componentBuildingDefinitionRegistry;
  }

  public ArtifactAstConfigurationBuilder(ArtifactAst artifactAst, Map<String, String> artifactProperties,
                                         ArtifactType artifactType, boolean enableLazyInit,
                                         ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry)
      throws ConfigurationException {
    this(artifactAst, artifactProperties, artifactType, enableLazyInit,
         DefaultMemoryManagementService.getInstance(),
         componentBuildingDefinitionRegistry);
  }

  public ArtifactAstConfigurationBuilder(ArtifactAst artifactAst, Map<String, String> artifactProperties)
      throws ConfigurationException {
    this(artifactAst, artifactProperties, ArtifactType.APP, false,
         new DefaultComponentBuildingDefinitionRegistryFactory()
             .create(artifactAst.dependencies(),
                     artifactAst::dependenciesDsl));
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    if (emptyArtifact().equals(artifactAst) && artifactType == DOMAIN) {
      ((DefaultZernovaContext) muleContext).setLifecycleStrategy(new NullDomainZernovaContextLifecycleStrategy());
      return;
    }

    initialiseIfNeeded(this, muleContext);
    Optional<ConfigurationProperties> parentConfigurationProperties = resolveParentConfigurationProperties();

    final BaseZernovaArtifactContext baseMuleArtifactContext = createBaseContext(muleContext, parentConfigurationProperties);
    serviceConfigurators.forEach(serviceConfigurator -> serviceConfigurator.configure(muleContext.getCustomizationService()));
    BaseSpringRegistry baseRegistry = createBaseRegistry(muleContext, baseMuleArtifactContext);

    try {
      muleArtifactContext = createApplicationContext(muleContext,
                                                     baseMuleArtifactContext.getBean(BaseConfigurationComponentLocator.class),
                                                     baseMuleArtifactContext.getBean(ContributedErrorTypeRepository.class),
                                                     baseMuleArtifactContext.getBean(ContributedErrorTypeLocator.class),
                                                     baseMuleArtifactContext.getBean(FeatureFlaggingService.class),
                                                     baseMuleArtifactContext.getBean(ExpressionLanguageMetadataService.class));
      muleArtifactContext.setParent(baseMuleArtifactContext);
      createSpringRegistry((DefaultZernovaContext) muleContext, baseMuleArtifactContext, muleArtifactContext);
    } catch (Exception e) {
      baseRegistry.dispose();
      throw e;
    }
  }

  private BaseZernovaArtifactContext createBaseContext(ZernovaContext muleContext,
                                                    Optional<ConfigurationProperties> parentConfigurationProperties) {
    final BaseZernovaArtifactContext baseMuleArtifactContext = new BaseZernovaArtifactContext(muleContext,
                                                                                        parentConfigurationProperties,
                                                                                        getArtifactProperties(),
                                                                                        artifactType,
                                                                                        enableLazyInit);
    if (baseMuleArtifactContext instanceof ConfigurableApplicationContext) {
      baseMuleArtifactContext.setParent(parentContext);
    }
    return baseMuleArtifactContext;
  }

  private BaseSpringRegistry createBaseRegistry(ZernovaContext muleContext, final BaseZernovaArtifactContext baseMuleArtifactContext)
      throws InitialisationException {
    BaseSpringRegistry baseRegistry = new BaseSpringRegistry(baseMuleArtifactContext, muleContext,
                                                             ((DefaultZernovaContext) muleContext).getLifecycleInterceptor());

    ((DefaultZernovaContext) muleContext).setRegistry(baseRegistry);
    ((DefaultZernovaContext) muleContext).setInjector(baseRegistry);
    baseRegistry.initialise();

    return baseRegistry;
  }

  private ZernovaArtifactContext createApplicationContext(ZernovaContext muleContext,
                                                       BaseConfigurationComponentLocator baseConfigurationComponentLocator,
                                                       ContributedErrorTypeRepository errorTypeRepository,
                                                       ContributedErrorTypeLocator errorTypeLocator,
                                                       FeatureFlaggingService featureFlaggingService,
                                                       ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws Exception {
    // TODO MULE-10084 : Refactor to only accept artifactConfiguration and not artifactConfigResources
    return doCreateApplicationContext(muleContext,
                                      baseConfigurationComponentLocator, errorTypeRepository, errorTypeLocator,
                                      featureFlaggingService, expressionLanguageMetadataService);
  }

  private ZernovaArtifactContext doCreateApplicationContext(ZernovaContext muleContext,
                                                         BaseConfigurationComponentLocator baseConfigurationComponentLocator,
                                                         ContributedErrorTypeRepository errorTypeRepository,
                                                         ContributedErrorTypeLocator errorTypeLocator,
                                                         FeatureFlaggingService featureFlaggingService,
                                                         ExpressionLanguageMetadataService expressionLanguageMetadataService) {
    if (enableLazyInit) {
      return new LazyZernovaArtifactContext(muleContext, artifactAst,
                                         resolveParentConfigurationProperties(),
                                         baseConfigurationComponentLocator,
                                         errorTypeRepository, errorTypeLocator,
                                         getArtifactProperties(), artifactType,
                                         resolveComponentModelInitializer(),
                                         componentBuildingDefinitionRegistry,
                                         new ArtifactMemoryManagementService(memoryManagementService),
                                         featureFlaggingService, expressionLanguageMetadataService);
    } else {
      ZernovaArtifactContext context;
      context = new ZernovaArtifactContext(muleContext, artifactAst,
                                        resolveParentConfigurationProperties(),
                                        baseConfigurationComponentLocator,
                                        errorTypeRepository, errorTypeLocator,
                                        getArtifactProperties(), artifactType,
                                        componentBuildingDefinitionRegistry,
                                        new ArtifactMemoryManagementService(memoryManagementService),
                                        featureFlaggingService, expressionLanguageMetadataService);
      context.initialize();
      return context;
    }
  }

  private Optional<ConfigurationProperties> resolveParentConfigurationProperties() {
    Optional<ConfigurationProperties> parentConfigurationProperties = empty();
    if (parentContext != null) {
      parentConfigurationProperties = of(parentContext.getBean(ConfigurationProperties.class));
    }

    return parentConfigurationProperties;
  }

  private Optional<ComponentModelInitializer> resolveComponentModelInitializer() {
    Optional<ComponentModelInitializer> parentLazyComponentInitializer = empty();
    if (parentContext instanceof ComponentModelInitializer) {
      parentLazyComponentInitializer = of((ComponentModelInitializer) parentContext);
    }

    return parentLazyComponentInitializer;
  }

  private void createSpringRegistry(DefaultZernovaContext muleContext, ApplicationContext baseApplicationContext,
                                    ZernovaArtifactContext applicationContext)
      throws Exception {
    SpringRegistry registry;

    if (parentContext != null) {
      registry = createRegistryWithParentContext(muleContext, baseApplicationContext, applicationContext);
    } else {
      registry = new SpringRegistry(baseApplicationContext, applicationContext, muleContext,
                                    new ConfigurationDependencyResolver(applicationContext.getApplicationModel()),
                                    muleContext.getLifecycleInterceptor());
    }

    muleContext.setRegistry(registry);
    muleContext.setInjector(registry);
  }

  private SpringRegistry createRegistryWithParentContext(ZernovaContext muleContext,
                                                         ApplicationContext baseApplicationContext,
                                                         ZernovaArtifactContext applicationContext)
      throws ConfigurationException {
    if (baseApplicationContext instanceof ConfigurableApplicationContext) {
      return new SpringRegistry(baseApplicationContext, applicationContext, muleContext,
                                new ConfigurationDependencyResolver(applicationContext.getApplicationModel()),
                                ((DefaultZernovaContext) muleContext).getLifecycleInterceptor());
    } else {
      throw new ConfigurationException(createStaticMessage("Cannot set a parent context if the ApplicationContext does not implement ConfigurableApplicationContext"));
    }
  }

  @Override
  protected synchronized void applyLifecycle(LifecycleManager lifecycleManager) throws Exception {
    // If the ZernovaContext is started, start all objects in the new Registry.
    if (lifecycleManager.isPhaseComplete(Startable.PHASE_NAME)) {
      lifecycleManager.fireLifecycle(Startable.PHASE_NAME);
    }
  }

  @Override
  public ArtifactContext createArtifactContext() {
    return new SpringArtifactContext(muleArtifactContext);
  }

  @Override
  public void setParentContext(ZernovaContext domainContext, ArtifactAst parentAst) {
    this.parentContext = ((ZernovaContextWithRegistry) domainContext).getRegistry().get(SPRING_APPLICATION_CONTEXT);
  }

  public Map<String, String> getArtifactProperties() {
    return artifactProperties;
  }
}
