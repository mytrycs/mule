/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.descriptor;

import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.ARTIFACT_DESCRIPTORS_WITH_CUSTOM_LOG_CONFIG;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.qameta.allure.Story;

@Story(ARTIFACT_DESCRIPTORS_WITH_CUSTOM_LOG_CONFIG)
@RunWith(Parameterized.class)
public class DeployableDescriptorsWithCustomLogConfig extends AbstractDeployableArtifactDescriptorFactoryTestCase {

  private final String deployableProjectFolder;
  private final Boolean isApplication;

  @Rule
  public final SystemProperty muleHomeSystemProperty;

  @Parameterized.Parameters(name = "Deployable project folder: {0}, Custom log config file path: {1}")
  public static Collection<Object[]> deployableArtifactDescriptors() {
    return asList(new Object[][] {
        {"apps/with-custom-log-config", "some-path"},
        {"domains/with-custom-log-config", "some-path"},
        {"apps/with-custom-log-config", null},
        {"domains/with-custom-log-config", null}});
  }

  public DeployableDescriptorsWithCustomLogConfig(String deployableProjectFolder, String customLogConfigFilePath) {
    this.deployableProjectFolder = deployableProjectFolder;
    isApplication = deployableProjectFolder.startsWith("apps");

    muleHomeSystemProperty =
        new SystemProperty(MULE_HOME_DIRECTORY_PROPERTY, customLogConfigFilePath);
  }

  @Test
  public void createDeployableDescriptorWithCustomLogConfig() throws URISyntaxException {
    DeployableArtifactDescriptor deployableArtifactDescriptor = getDeployableArtifactDescriptor();

    String base = muleHomeSystemProperty.getValue() != null ? muleHomeSystemProperty.getValue() : ".";
    Path customLogConfig = Paths.get(base, "custom-log4j2.xml");
    assertThat(deployableArtifactDescriptor.getLogConfigFile().toPath(), is(customLogConfig));
  }

  private DeployableArtifactDescriptor getDeployableArtifactDescriptor() throws URISyntaxException {
    if (isApplication) {
      return createApplicationDescriptor(deployableProjectFolder);
    } else {
      return createDomainDescriptor(deployableProjectFolder);
    }
  }

}
