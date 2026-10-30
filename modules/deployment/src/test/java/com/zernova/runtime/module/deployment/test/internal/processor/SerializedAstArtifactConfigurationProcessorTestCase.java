/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal.processor;

import static com.zernova.runtime.ast.api.ArtifactType.APPLICATION;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.SERIALIZED_ARTIFACT_AST_LOCATION;
import static com.zernova.tck.util.ZernovaContextUtils.addExtensionModelToMock;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;
import static com.zernova.test.allure.AllureConstants.ArtifactDeploymentFeature.DOMAIN_DEPLOYMENT;
import static com.zernova.test.allure.AllureConstants.ArtifactDeploymentFeature.DeploymentFailureStory.DEPLOYMENT_FAILURE;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContextConfiguration;
import com.zernova.runtime.module.deployment.internal.processor.SerializedAstArtifactConfigurationProcessor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(DOMAIN_DEPLOYMENT)
@Story(DEPLOYMENT_FAILURE)
public class SerializedAstArtifactConfigurationProcessorTestCase extends AbstractZernovaTestCase {

  private SerializedAstArtifactConfigurationProcessor configurationBuilder;
  private ZernovaContext muleContext;

  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void setUp() throws Exception {
    muleContext = mockContextWithServices();
    addExtensionModelToMock(muleContext, getExtensionModel());
    muleContext.getInjector().inject(this);
    configurationBuilder = new SerializedAstArtifactConfigurationProcessor();
  }

  @Test
  @Issue("W-12374722")
  public void serializedAstWithIncorrectArtifactType() throws ConfigurationException {
    when(muleContext.getExecutionClassLoader()).thenReturn(new ClassLoader() {

      @Override
      public InputStream getResourceAsStream(String name) {
        if (SERIALIZED_ARTIFACT_AST_LOCATION.equals(name)) {
          return toInputStream("JSON#1.0#UTF-8#\n" +
              "{\"artifactName\":\"artifact\",\"artifactType\":\"" + APPLICATION.name() + "\"," +
              "\"dependencies\":[{\"name\":\"mule\"}]," +
              "  \"topLevelComponentAsts\": []," +
              "  \"errorTypeRepository\": {\"hierarchy\": []}," +
              "  \"importedResources\": []}",
                               UTF_8);
        } else {
          return SerializedAstArtifactConfigurationProcessorTestCase.class.getClassLoader().getResourceAsStream(name);
        }
      }
    });

    expectedException.expect(ConfigurationException.class);
    expectedException.expectCause(instanceOf(IllegalStateException.class));
    expectedException
        .expectMessage("Expected artifact type '" + DOMAIN.name() + "' but serialized ast was '" + APPLICATION.name() + "'");

    configurationBuilder.createArtifactContext(ArtifactContextConfiguration.builder()
        .setConfigResources(new String[] {"mule-config.xml"})
        .setArtifactType(DOMAIN)
        .setMuleContext(muleContext)
        .build());
  }


}
