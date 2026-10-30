/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.registry;

import static com.zernova.runtime.ast.api.ArtifactType.APPLICATION;
import static com.zernova.runtime.config.internal.error.ZernovaCoreErrorTypeRepository.MULE_CORE_ERROR_TYPE_REPOSITORY;
import static com.zernova.runtime.core.api.config.ZernovaDeploymentProperties.MULE_ADD_ARTIFACT_AST_TO_REGISTRY_DEPLOYMENT_PROPERTY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ARTIFACT_AST;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.test.allure.AllureConstants.ArtifactAst.ARTIFACT_AST;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.ObjectRegistrationStory.OBJECT_REGISTRATION;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import static org.slf4j.LoggerFactory.getLogger;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import com.zernova.runtime.config.internal.ArtifactAstConfigurationBuilder;
import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.config.TestServicesConfigurationBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.MockExtensionManagerConfigurationBuilder;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import io.qameta.allure.Feature;
import io.qameta.allure.Features;
import io.qameta.allure.Story;

@Features({@Feature(ARTIFACT_AST), @Feature(REGISTRY)})
@Story(OBJECT_REGISTRATION)
@RunWith(Parameterized.class)
public class ArtifactAstInRegistryTestCase extends AbstractZernovaTestCase {

  private static final Logger LOGGER = getLogger(ArtifactAstInRegistryTestCase.class);

  @Parameters(name = "addArtifactAstToRegistry: {0}; lazyInit: {1}")
  public static List<Boolean[]> params() {
    return asList(new Boolean[][] {
        {false, false},
        {true, false},
        {false, true},
        {true, true}
    });
  }

  @Parameter(0)
  public Boolean addArtifactAstToRegistry;

  @Parameter(1)
  public Boolean lazyInit;

  @Rule
  public TestServicesConfigurationBuilder testServicesConfigurationBuilder = new TestServicesConfigurationBuilder();

  private ArtifactAst artifactAst;

  private ZernovaContextWithRegistry muleContext;

  @Before
  public void before() throws Exception {
    artifactAst = mock(ArtifactAst.class);
    when(artifactAst.getArtifactName()).thenReturn("my mock ast");
    when(artifactAst.getArtifactType()).thenReturn(APPLICATION);
    when(artifactAst.getErrorTypeRepository()).thenReturn(MULE_CORE_ERROR_TYPE_REPOSITORY);
    when(artifactAst.enrichedErrorTypeRepository()).thenReturn(MULE_CORE_ERROR_TYPE_REPOSITORY);
    ArtifactAstConfigurationBuilder artifactAstConfigurationBuilder = new ArtifactAstConfigurationBuilder(artifactAst,
                                                                                                          singletonMap(MULE_ADD_ARTIFACT_AST_TO_REGISTRY_DEPLOYMENT_PROPERTY,
                                                                                                                       addArtifactAstToRegistry
                                                                                                                           .toString()),
                                                                                                          APP,
                                                                                                          lazyInit,
                                                                                                          mock(ComponentBuildingDefinitionRegistry.class));

    muleContext = (ZernovaContextWithRegistry) new DefaultZernovaContextFactory()
        .createMuleContext(testServicesConfigurationBuilder,
                           new MockExtensionManagerConfigurationBuilder(),
                           artifactAstConfigurationBuilder);
    muleContext.start();
  }

  @After
  public void after() {
    if (muleContext != null) {
      disposeIfNeeded(muleContext, LOGGER);
    }
  }

  @Test
  public void artifactAstAvailableInRegistryIfEnabled() {
    ArtifactAst actualArtifactAst = muleContext.getRegistry().lookupObject(OBJECT_ARTIFACT_AST);
    if (addArtifactAstToRegistry) {
      assertThat(actualArtifactAst.getArtifactName(), is("my mock ast"));
    } else {
      assertThat(actualArtifactAst, is(nullValue()));
    }
  }
}
