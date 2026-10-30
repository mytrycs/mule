/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4;

import static com.zernova.functional.junit4.FunctionalTestCase.extensionManagerWithMuleExtModelBuilder;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.ast.api.ArtifactType;
import com.zernova.runtime.config.api.ArtifactContextFactory;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.api.context.ZernovaContextBuilder;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.tck.config.TestPolicyProviderConfigurationBuilder;
import com.zernova.tck.config.TestServicesConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DomainContextBuilder {

  private String contextId;
  private String[] domainConfig = new String[0];
  private ArtifactCoordinates artifactCoordinates;
  private boolean isEnableLazyInit = false;
  private boolean isDisableXmlValidations = false;

  private TestServicesConfigurationBuilder testServicesConfigBuilder;

  private final ZernovaContextBuilder muleContextBuilder = ZernovaContextBuilder.builder(DOMAIN);

  public DomainContextBuilder setContextId(String contextId) {
    this.contextId = contextId;
    return this;
  }

  public DomainContextBuilder setDomainConfig(String... domainConfig) {
    this.domainConfig = domainConfig;
    return this;
  }

  /**
   * Controls whether lazy initialization of components is enabled/disabled.
   *
   * @param enableLazyInit whether lazy initialization is enabled.
   * @since 4.5
   */
  public DomainContextBuilder setEnableLazyInit(boolean enableLazyInit) {
    isEnableLazyInit = enableLazyInit;
    return this;
  }

  /**
   * Controls whether if XML validations should be done over the configuration file.
   *
   * @param disableXmlValidations whether XML validations are disabled.
   * @since 4.5
   */
  public DomainContextBuilder setDisableXmlValidations(boolean disableXmlValidations) {
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
  public DomainContextBuilder setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    this.artifactCoordinates = artifactCoordinates;
    return this;
  }

  public ArtifactContext build() throws Exception {
    List<ConfigurationBuilder> builders = new ArrayList<>(4);
    ConfigurationBuilder cfgBuilder = getDomainBuilder(domainConfig);
    testServicesConfigBuilder = new TestServicesConfigurationBuilder();
    cfgBuilder.addServiceConfigurator(testServicesConfigBuilder);
    builders.add(extensionManagerWithMuleExtModelBuilder(getExtensionModels()));
    builders.add(new TestPolicyProviderConfigurationBuilder());
    builders.add(cfgBuilder);
    addBuilders(builders);
    final DefaultZernovaConfiguration muleConfiguration = new DefaultZernovaConfiguration();
    if (contextId != null) {
      muleConfiguration.setId(contextId);
    }
    muleContextBuilder.setMuleConfiguration(muleConfiguration);
    muleContextBuilder.setArtifactCoordinates(artifactCoordinates);
    DefaultZernovaContextFactory muleContextFactory = new DefaultZernovaContextFactory();
    ZernovaContext domainContext = muleContextFactory.createMuleContext(builders, muleContextBuilder);
    domainContext.start();

    ZernovaContextNotificationListener listener = notification -> {
      try {
        testServicesConfigBuilder.stopServices();
      } catch (ZernovaException e) {
        throw new ZernovaRuntimeException(e);
      }
    };
    domainContext.getNotificationManager().addListener(listener);

    return ((ArtifactContextFactory) cfgBuilder).createArtifactContext();
  }

  protected void addBuilders(List<ConfigurationBuilder> builders) {
    builders.add(testServicesConfigBuilder);
  }

  private ConfigurationBuilder getDomainBuilder(String[] configResources) throws Exception {
    ArtifactAstXmlParserConfigurationBuilder appBuilder =
        new ArtifactAstXmlParserConfigurationBuilder(emptyMap(),
                                                     isDisableXmlValidations,
                                                     isEnableLazyInit,
                                                     false,
                                                     false,
                                                     configResources,
                                                     getExpressionLanguageMetadataService());
    appBuilder.setArtifactType(ArtifactType.DOMAIN);
    return appBuilder;
  }

  protected ExpressionLanguageMetadataService getExpressionLanguageMetadataService() {
    return mock(ExpressionLanguageMetadataService.class);
  }

  protected Set<ExtensionModel> getExtensionModels() {
    return singleton(getExtensionModel());
  }
}
