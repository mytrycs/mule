/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal.processor;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENTITY_RESOLVER_FAIL_ON_FIRST_ERROR;
import static com.zernova.runtime.config.internal.context.BaseSpringZernovaContextServiceConfigurator.DISABLE_TRANSFORMERS_SUPPORT;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.tck.util.ZernovaContextUtils.addExtensionModelToMock;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;

import static java.util.Optional.of;

import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContextConfiguration;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator;
import com.zernova.runtime.module.deployment.internal.processor.AstXmlParserArtifactConfigurationProcessor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import jakarta.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.qameta.allure.Issue;

public class AstXmlParserArtifactConfigurationProcessorTestCase extends AbstractZernovaTestCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  public static final String SCHEMA_VALIDATION_ERROR =
      "Can't resolve https://schemas.zernova.dev/invalid-namespace/current/invalid-schema.xsd, A dependency or plugin might be missing";
  @Inject
  private FeatureFlaggingService featureFlaggingService;

  private AstXmlParserArtifactConfigurationProcessor configurationBuilder;
  private ZernovaContext muleContext;

  @Rule
  public SystemProperty disableExpressionsSupport = new SystemProperty(DISABLE_TRANSFORMERS_SUPPORT, "true");

  @Before
  public void setUp() throws Exception {
    muleContext = mockContextWithServices();
    addExtensionModelToMock(muleContext, getExtensionModel());
    muleContext.getInjector().inject(this);
    configurationBuilder = new AstXmlParserArtifactConfigurationProcessor();
  }

  @Test
  @Issue("MULE-19534")
  public void configureWithFailOnFirstError() throws ConfigurationException {
    when(featureFlaggingService.isEnabled(ENTITY_RESOLVER_FAIL_ON_FIRST_ERROR)).thenReturn(true);

    final ArtifactContextConfiguration artifactContextConfiguration = ArtifactContextConfiguration.builder()
        .setConfigResources(new String[] {"invalid-schema.xml"})
        .setArtifactType(APP)
        .setMuleContext(muleContext)
        .setEnableLazyInitialization(false)
        .setDisableXmlValidations(false)
        .build();
    var thrown = assertThrows(ConfigurationException.class,
                              () -> configurationBuilder.createArtifactContext(artifactContextConfiguration));
    assertThat(thrown.getMessage(), containsString(SCHEMA_VALIDATION_ERROR));
  }

  @Test
  @Issue("MULE-19534")
  public void configureWithFailAfterTenErrors() throws ConfigurationException {
    when(featureFlaggingService.isEnabled(ENTITY_RESOLVER_FAIL_ON_FIRST_ERROR)).thenReturn(false);

    final ArtifactContextConfiguration artifactContextConfiguration = ArtifactContextConfiguration.builder()
        .setConfigResources(new String[] {"invalid-schema.xml"})
        .setArtifactType(APP)
        .setMuleContext(muleContext)
        .setEnableLazyInitialization(false)
        .setDisableXmlValidations(false)
        .build();
    var thrown = assertThrows(ConfigurationException.class,
                              () -> configurationBuilder.createArtifactContext(artifactContextConfiguration));
    assertThat(thrown.getMessage(), containsString(SCHEMA_VALIDATION_ERROR));
  }

  @Test
  @Issue("MULE-19534")
  public void configureWithFailAfterTenErrorsWillSucceedIfSchemaNotUsed() throws ConfigurationException {
    when(featureFlaggingService.isEnabled(ENTITY_RESOLVER_FAIL_ON_FIRST_ERROR)).thenReturn(false);

    ArtifactContext context = configurationBuilder.createArtifactContext(ArtifactContextConfiguration.builder()
        .setConfigResources(new String[] {"invalid-schema-not-used.xml"})
        .setArtifactType(APP)
        .setMuleContext(muleContext)
        .setEnableLazyInitialization(false)
        .setDisableXmlValidations(false)
        .build());

    assertThat(context.getArtifactAst(), not(nullValue()));
    assertThat(context.getMuleContext(), sameInstance(muleContext));
  }

  @Test
  @Issue("MULE-19791")
  public void configureWithResourceOutsideClasspathPreservesResourceName() throws ConfigurationException, IOException {
    copyResourceToTemp("simple.xml");

    final ArtifactContext artifactContext =
        withContextClassLoader(new URLClassLoader(new URL[] {tempFolder.getRoot().toURI().toURL()}, null),
                               () -> configurationBuilder.createArtifactContext(ArtifactContextConfiguration.builder()
                                   .setConfigResources(new String[] {"simple.xml"})
                                   .setArtifactType(APP)
                                   .setMuleContext(muleContext)
                                   .setEnableLazyInitialization(false)
                                   .setDisableXmlValidations(false)
                                   .build()));
    final ArtifactAst artifactAst = artifactContext.getArtifactAst();
    final ComponentAst componentAst = artifactAst.topLevelComponents().get(0);
    assertThat(componentAst.getMetadata().getFileName(), is(of("simple.xml")));
  }

  private void copyResourceToTemp(String resourceName) throws IOException {
    final URL originalResource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
    final File simpleAppFileOutsideClassPath = new File(tempFolder.getRoot(), resourceName);
    assertThat(originalResource, is(not(nullValue())));
    copyURLToFile(originalResource, simpleAppFileOutsideClassPath);
  }

  public static final class TestExtensionSchemagenerator implements ExtensionSchemaGenerator {

    @Override
    public String generate(ExtensionModel extensionModel, DslResolvingContext context) {
      return "";
    }

    @Override
    public String generate(ExtensionModel extensionModel, DslResolvingContext context, DslSyntaxResolver dsl) {
      return "";
    }
  }

}
