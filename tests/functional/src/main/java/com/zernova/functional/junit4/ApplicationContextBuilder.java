/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4;

import static com.zernova.functional.junit4.FunctionalTestCase.extensionManagerWithMuleExtModelBuilder;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.api.context.ZernovaContextBuilder;
import com.zernova.runtime.core.api.context.ZernovaContextFactory;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.tck.config.TestPolicyProviderConfigurationBuilder;
import com.zernova.tck.config.TestServicesConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApplicationContextBuilder {

  private String contextId;
  private ArtifactContext domainArtifactContext;
  private String[] applicationResources = new String[0];
  private ArtifactCoordinates artifactCoordinates;
  private boolean isEnableLazyInit = false;
  private boolean isDisableXmlValidations = false;

  private TestServicesConfigurationBuilder testServicesConfigBuilder;

  private final ZernovaContextBuilder muleContextBuilder = ZernovaContextBuilder.builder(APP);
  private ExpressionLanguageMetadataService expressionLanguageMetadataService = mock(ExpressionLanguageMetadataService.class);

  public ApplicationContextBuilder setContextId(String contextId) {
    this.contextId = contextId;
    return this;
  }

  public ApplicationContextBuilder setDomainArtifactContext(ArtifactContext domainArtifactContext) {
    this.domainArtifactContext = domainArtifactContext;
    return this;
  }

  public ApplicationContextBuilder setApplicationResources(String... applicationResources) {
    this.applicationResources = applicationResources;
    return this;
  }

  /**
   * Controls whether lazy initialization of components is enabled/disabled.
   *
   * @param enableLazyInit whether lazy initialization is enabled.
   * @since 4.5
   */
  public ApplicationContextBuilder setEnableLazyInit(boolean enableLazyInit) {
    isEnableLazyInit = enableLazyInit;
    return this;
  }

  /**
   * Controls whether if XML validations should be done over the configuration file.
   *
   * @param disableXmlValidations whether XML validations are disabled.
   * @since 4.5
   */
  public ApplicationContextBuilder setDisableXmlValidations(boolean disableXmlValidations) {
    isDisableXmlValidations = disableXmlValidations;
    return this;
  }

  /**
   * Set's the application's {@link ArtifactCoordinates}
   *
   * @param artifactCoordinates the app's {@link ArtifactCoordinates}
   * @return {@code this} builder
   * @since 4.5.0
   */
  public ApplicationContextBuilder setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    this.artifactCoordinates = artifactCoordinates;
    return this;
  }

  public ApplicationContextBuilder setExpressionLanguageMetadataService(ExpressionLanguageMetadataService expressionLanguageMetadataService) {
    this.expressionLanguageMetadataService = expressionLanguageMetadataService;
    return this;
  }

  public ZernovaContext build() throws Exception {
    // Should we set up the manager for every method?
    ZernovaContext context = doBuildContext();
    context.start();
    return context;
  }

  protected ZernovaContext doBuildContext() throws Exception {
    ZernovaContext context;
    ZernovaContextFactory muleContextFactory = new DefaultZernovaContextFactory();
    List<ConfigurationBuilder> builders = new ArrayList<>(3);
    builders.add(extensionManagerWithMuleExtModelBuilder(getExtensionModels()));
    ConfigurationBuilder appBuilder = getAppBuilder(this.applicationResources);
    testServicesConfigBuilder = new TestServicesConfigurationBuilder();
    appBuilder.addServiceConfigurator(testServicesConfigBuilder);
    builders.add(appBuilder);
    builders.add(new TestPolicyProviderConfigurationBuilder());
    addBuilders(builders);
    final DefaultZernovaConfiguration muleConfiguration = new DefaultZernovaConfiguration();
    if (contextId != null) {
      muleConfiguration.setId(contextId);
    }
    muleContextBuilder.setMuleConfiguration(muleConfiguration);
    muleContextBuilder.setArtifactCoordinates(artifactCoordinates);
    configureMuleContext(muleContextBuilder);
    context = muleContextFactory.createMuleContext(builders, muleContextBuilder);
    return context;
  }

  protected Set<ExtensionModel> getExtensionModels() {
    return singleton(getExtensionModel());
  }

  // This shouldn't be needed by Test cases but can be used by base testcases that wish to add further builders when
  // creating the ZernovaContext.
  protected void addBuilders(List<ConfigurationBuilder> builders) {
    // No op
  }

  protected ConfigurationBuilder getAppBuilder(String[] configResources) throws Exception {
    ArtifactAstXmlParserConfigurationBuilder appBuilder =
        new ArtifactAstXmlParserConfigurationBuilder(emptyMap(),
                                                     isDisableXmlValidations,
                                                     isEnableLazyInit,
                                                     false,
                                                     false,
                                                     configResources,
                                                     expressionLanguageMetadataService);
    appBuilder.setParentArtifactContext(domainArtifactContext);
    return appBuilder;
  }

  /**
   * Override this method to set properties of the ZernovaContextBuilder before it is used to create the ZernovaContext.
   */
  protected void configureMuleContext(ZernovaContextBuilder contextBuilder) {}
}
