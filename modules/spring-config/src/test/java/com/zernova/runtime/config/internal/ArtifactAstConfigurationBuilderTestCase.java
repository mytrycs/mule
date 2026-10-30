/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal;

import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;
import static com.zernova.runtime.config.internal.context.BaseSpringZernovaContextServiceConfigurator.DISABLE_TRANSFORMERS_SUPPORT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.tck.util.ZernovaContextUtils.getRegistry;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;

import static java.util.Collections.emptyMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.junit.Assert.fail;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import com.zernova.runtime.config.internal.lazy.LazyExpressionLanguageAdaptor;
import com.zernova.runtime.config.internal.registry.BaseSpringRegistry;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.el.ExpressionLanguageAdaptor;
import com.zernova.runtime.core.internal.el.dataweave.DataWeaveExpressionLanguageAdaptor;
import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import io.qameta.allure.Issue;
import jakarta.inject.Inject;

public class ArtifactAstConfigurationBuilderTestCase extends AbstractZernovaTestCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  public static final String SCHEMA_VALIDATION_ERROR =
      "Can't resolve https://schemas.zernova.dev/invalid-namespace/current/invalid-schema.xsd, A dependency or plugin might be missing";

  private ZernovaContext muleContext;

  @Rule
  public SystemProperty disableExpressionsSupport = new SystemProperty(DISABLE_TRANSFORMERS_SUPPORT, "true");

  @Before
  public void setUp() throws Exception {
    muleContext = mockContextWithServices();
    muleContext.getInjector().inject(this);
  }

  @Test
  @Issue("W-11745207")
  public void baseRegistryWithLazyInitialisation() throws Exception {
    doTestBaseRegistryExpressionLanguageAdapter(true, LazyExpressionLanguageAdaptor.class);
  }

  @Test
  @Issue("W-11745207")
  public void baseRegistryWithEagerInitialisation() throws Exception {
    doTestBaseRegistryExpressionLanguageAdapter(false, DataWeaveExpressionLanguageAdaptor.class);
  }

  private void doTestBaseRegistryExpressionLanguageAdapter(boolean lazyInit, Class expectedClass) throws IOException,
      ConfigurationException {
    final ArtifactAstConfigurationBuilder configurationBuilder =
        astConfigurationBuilderRelativeToPath(tempFolder.getRoot(), emptyArtifact(), lazyInit);
    configurationBuilder.configure(muleContext);

    BaseSpringRegistry baseSpringRegistry = getRegistry(muleContext, BaseSpringRegistry.class);
    ExpressionLanguageAdaptor dataWeaveExpressionLanguageAdaptor =
        baseSpringRegistry.get(OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER);

    assertThat(dataWeaveExpressionLanguageAdaptor, is(notNullValue()));
    assertThat(dataWeaveExpressionLanguageAdaptor, instanceOf(expectedClass));
  }

  @Test
  public void memoryManagementCanBeInjectedInBean() throws ZernovaException, IOException {
    final ArtifactAstConfigurationBuilder configurationBuilder =
        astConfigurationBuilderRelativeToPath(tempFolder.getRoot(), emptyArtifact(), false);

    configurationBuilder.configure(muleContext);
    final ArtifactContext artifactContext = configurationBuilder.createArtifactContext();
    MemoryManagementInjected memoryManagementInjected = new MemoryManagementInjected();
    artifactContext.getMuleContext().getInjector().inject(memoryManagementInjected);

    assertThat(memoryManagementInjected.getMemoryManagementService(), is(notNullValue()));
  }

  @Test
  @Issue("W-13969259")
  public void baseRegistryDisposedOnDeploymentError() throws IOException {
    final ArtifactAstConfigurationBuilder configurationBuilder =
        astConfigurationBuilderRelativeToPath(tempFolder.getRoot(), emptyArtifact(), false);

    final ArgumentCaptor<Registry> registryCaptor = forClass(Registry.class);
    doNothing()
        .when((DefaultZernovaContext) muleContext)
        .setRegistry(registryCaptor.capture());

    doThrow(IllegalStateException.class)
        .when((DefaultZernovaContext) muleContext)
        // some method called within the configuration that uses the baseRegistry
        .getExtensionManager();

    try {
      configurationBuilder.configure(muleContext);
      fail("Expected IllegalStateException");
    } catch (ConfigurationException e) {
      assertThat(e.getCause(), instanceOf(IllegalStateException.class));
      final BaseSpringRegistry baseRegistry = (BaseSpringRegistry) (registryCaptor.getValue());
      assertThat(baseRegistry.getLifecycleManager().getLastExecutedPhase(), is(Disposable.PHASE_NAME));
    }
  }

  private ArtifactAstConfigurationBuilder astConfigurationBuilderRelativeToPath(File basePath, ArtifactAst artifactAst,
                                                                                boolean lazyInit)
      throws IOException {
    return withContextClassLoader(new URLClassLoader(new URL[] {basePath.toURI().toURL()}, null),
                                  () -> new ArtifactAstConfigurationBuilder(artifactAst, emptyMap(), APP, lazyInit,
                                                                            mock(ComponentBuildingDefinitionRegistry.class)));
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


  /**
   * Class to test the injection of a {@link MemoryManagementService}
   */
  private static class MemoryManagementInjected {

    @Inject
    private MemoryManagementService memoryManagementService;

    public MemoryManagementService getMemoryManagementService() {
      return memoryManagementService;
    }
  }
}
