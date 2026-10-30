/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.plugin;

import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.test.allure.AllureConstants.ArtifactPatchingFeature.ARTIFACT_PATCHING;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.PATCHED_ARTIFACT_DESCRIPTORS;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DomainDescriptorResolver;
import com.zernova.runtime.module.artifact.activation.internal.descriptor.DefaultDeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.internal.maven.MavenDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@Feature(ARTIFACT_PATCHING)
@Story(PATCHED_ARTIFACT_DESCRIPTORS)
@RunWith(Parameterized.class)
public class DeployableDescriptorsWithPatchedPluginsTestCase extends AbstractZernovaTestCase {

  private static final String PATCHES_LOCATION = "/lib/patches/mule-artifact-patches";

  private final String deployableProjectFolder;
  private final Boolean isApplication;

  @Rule
  public final SystemProperty muleHomeSystemProperty;

  @Parameters(name = "Deployable project folder: {0}")
  public static Collection<String> deployableArtifactDescriptors() {
    return asList("apps/with-patched-artifacts", "domains/with-patched-artifacts");
  }

  public DeployableDescriptorsWithPatchedPluginsTestCase(String deployableProjectFolder)
      throws URISyntaxException, IOException {
    this.deployableProjectFolder = deployableProjectFolder;
    isApplication = deployableProjectFolder.startsWith("apps");

    muleHomeSystemProperty =
        new SystemProperty(MULE_HOME_DIRECTORY_PROPERTY, getDeployableFolder(deployableProjectFolder).getCanonicalPath());
  }

  @Test
  public void createDeployableDescriptorWithPatchedPlugins() throws URISyntaxException {
    DeployableArtifactDescriptor deployableArtifactDescriptor = getDeployableArtifactDescriptor();

    ArtifactPluginDescriptor httpPlugin = deployableArtifactDescriptor.getPlugins()
        .stream()
        .filter(p -> p.getName().equals("HTTP"))
        .findFirst()
        .get();

    assertThat(stream(httpPlugin.getClassLoaderConfiguration().getUrls()).collect(toList()),
               hasItem(getClass().getClassLoader().getResource(deployableProjectFolder + PATCHES_LOCATION + "/http-patch.jar")));

    ArtifactPluginDescriptor dbPlugin = deployableArtifactDescriptor.getPlugins()
        .stream()
        .filter(p -> p.getName().equals("Database"))
        .findFirst()
        .get();

    assertThat(stream(dbPlugin.getClassLoaderConfiguration().getUrls()).map(URL::toString).collect(toList()),
               not(hasItem(getClass().getClassLoader()
                   .getResource(deployableProjectFolder + PATCHES_LOCATION + "/db-patch.jar"))));
  }

  private DeployableArtifactDescriptor getDeployableArtifactDescriptor() throws URISyntaxException {
    if (isApplication) {
      return createApplicationDescriptor(deployableProjectFolder);
    } else {
      return createDomainDescriptor(deployableProjectFolder);
    }
  }

  protected static final DeployableArtifactDescriptorFactory deployableArtifactDescriptorFactory =
      new DefaultDeployableArtifactDescriptorFactory();

  protected DomainDescriptor createDomainDescriptor(String domainPath) throws URISyntaxException {
    DeployableProjectModel model = getDeployableProjectModel(domainPath);

    return deployableArtifactDescriptorFactory.createDomainDescriptor(model, emptyMap());
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath) throws URISyntaxException {
    return createApplicationDescriptor(appPath, null);
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath, DomainDescriptorResolver domainDescriptorResolver)
      throws URISyntaxException {
    DeployableProjectModel model = getDeployableProjectModel(appPath);

    return deployableArtifactDescriptorFactory.createApplicationDescriptor(model, emptyMap(),
                                                                           domainDescriptorResolver);
  }

  protected DeployableProjectModel getDeployableProjectModel(String deployablePath) throws URISyntaxException {
    return new MavenDeployableProjectModelBuilder(getDeployableFolder(deployablePath)).build();
  }

  protected File getDeployableFolder(String appPath) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(appPath).toURI());
  }


}
