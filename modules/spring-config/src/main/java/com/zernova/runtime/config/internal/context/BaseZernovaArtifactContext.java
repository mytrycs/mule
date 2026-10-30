/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import static com.zernova.runtime.config.internal.model.properties.PropertiesHierarchyCreationUtils.createConfigurationAttributeResolver;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONTEXT;

import static java.lang.String.format;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.config.internal.model.dsl.ClassLoaderResourceProvider;
import com.zernova.runtime.config.internal.model.dsl.config.PropertiesResolverConfigurationProperties;
import com.zernova.runtime.config.internal.processor.ZernovaInjectorProcessor;
import com.zernova.runtime.config.privileged.spring.ByteBuddySpringCacheInstrumentator;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.internal.config.InternalCustomizationService;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistryHelper;
import com.zernova.runtime.core.internal.registry.Registry;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;

/**
 * Spring context specialization that contains just some standard constant beans for every Mule deployable artifact.
 *
 * @since 4.5
 */
public class BaseZernovaArtifactContext extends AbstractRefreshableConfigApplicationContext {

  private final DefaultRegistry serviceDiscoverer;
  private final ZernovaContextWithRegistry muleContext;
  private final Registry originalRegistry;
  private final ArtifactType artifactType;
  private final PropertiesResolverConfigurationProperties configurationProperties;
  private final boolean enableLazyInit;

  static {
    ByteBuddySpringCacheInstrumentator.instrumentSpringCachesForCleanup();
  }

  /**
   * Configures the context.
   *
   * @param muleContext                   the {@link ZernovaContext} that own this context
   * @param parentConfigurationProperties optional {@link ConfigurationProperties}
   * @param artifactProperties            the artifact properties
   * @param artifactType                  the {@link ArtifactType}
   * @param enableLazyInit                whether lazy init is enabled
   */
  public BaseZernovaArtifactContext(ZernovaContext muleContext,
                                 Optional<ConfigurationProperties> parentConfigurationProperties,
                                 Map<String, String> artifactProperties, ArtifactType artifactType, boolean enableLazyInit) {
    this.muleContext = (ZernovaContextWithRegistry) muleContext;
    this.originalRegistry = ((ZernovaRegistryHelper) (((ZernovaContextWithRegistry) muleContext).getRegistry())).getDelegate();
    this.serviceDiscoverer = new DefaultRegistry(muleContext);
    this.artifactType = artifactType;

    final InternalCustomizationService customizationService =
        (InternalCustomizationService) (muleContext.getCustomizationService());
    customizationService.setArtifactProperties(artifactProperties);
    customizationService.setArtifactType(artifactType.getArtifactType());

    this.configurationProperties = createConfigurationAttributeResolver(parentConfigurationProperties,
                                                                        artifactProperties,
                                                                        new ClassLoaderResourceProvider(muleContext
                                                                            .getExecutionClassLoader()));

    this.enableLazyInit = enableLazyInit;
  }

  @Override
  protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    super.prepareBeanFactory(beanFactory);
    beanFactory.setBeanExpressionResolver(null);
    registerInjectorProcessor(beanFactory);
    beanFactory.addBeanPostProcessor(new ZernovaContextPostProcessor(muleContext));
    beanFactory.registerSingleton(OBJECT_MULE_CONTEXT, muleContext);
  }

  @Override
  protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
    // Nothing to do
  }

  @Override
  protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
    super.customizeBeanFactory(beanFactory);
    new BaseSpringZernovaContextServiceConfigurator(muleContext,
                                                 configurationProperties,
                                                 artifactType,
                                                 beanFactory,
                                                 serviceDiscoverer,
                                                 originalRegistry,
                                                 enableLazyInit)
                                                     .createArtifactServices();
  }

  private void registerInjectorProcessor(ConfigurableListableBeanFactory beanFactory) {
    ZernovaInjectorProcessor muleInjectorProcessor = new ZernovaInjectorProcessor();
    muleInjectorProcessor.setBeanFactory(beanFactory);
    beanFactory.addBeanPostProcessor(muleInjectorProcessor);
  }

  @Override
  protected DefaultListableBeanFactory createBeanFactory() {
    // Copy all postProcessors defined in the defaultMuleConfig so that they get applied to the child container
    DefaultListableBeanFactory beanFactory = new ObjectProviderAwareBeanFactory(getInternalParentBeanFactory());
    beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
    return beanFactory;
  }

  @Override
  public String toString() {
    return format("%s: %s (base)", this.getClass().getName(), muleContext.getConfiguration().getId());
  }

}
