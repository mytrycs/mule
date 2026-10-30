/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.descriptor;

import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.CLASSLOADING_ISOLATION;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DomainDescriptorResolver;
import com.zernova.runtime.module.artifact.activation.internal.maven.MavenDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.net.URISyntaxException;

import io.qameta.allure.Feature;

@Feature(CLASSLOADING_ISOLATION)
public class AbstractDeployableArtifactDescriptorFactoryTestCase extends AbstractZernovaTestCase {

  protected static final DeployableArtifactDescriptorFactory deployableArtifactDescriptorFactory =
      new DefaultDeployableArtifactDescriptorFactory();

  protected DomainDescriptor createDomainDescriptor(String domainPath) throws URISyntaxException {
    return createDomainDescriptor(domainPath, false);
  }

  protected DomainDescriptor createDomainDescriptor(String domainPath, boolean includeTestDependencies)
      throws URISyntaxException {
    DeployableProjectModel model = getDeployableProjectModel(domainPath, includeTestDependencies);

    return deployableArtifactDescriptorFactory.createDomainDescriptor(model, emptyMap());
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath) throws URISyntaxException {
    return createApplicationDescriptor(appPath, false, null);
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath, boolean includeTestDependencies)
      throws URISyntaxException {
    return createApplicationDescriptor(appPath, includeTestDependencies, null);
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath, DomainDescriptorResolver domainDescriptorResolver)
      throws URISyntaxException {
    return createApplicationDescriptor(appPath, false, domainDescriptorResolver);
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath, boolean includeTestDependencies,
                                                              DomainDescriptorResolver domainDescriptorResolver)
      throws URISyntaxException {
    DeployableProjectModel model = getDeployableProjectModel(appPath, includeTestDependencies);

    return deployableArtifactDescriptorFactory.createApplicationDescriptor(model, emptyMap(),
                                                                           domainDescriptorResolver);
  }

  protected DeployableProjectModel getDeployableProjectModel(String deployablePath) throws URISyntaxException {
    return getDeployableProjectModel(deployablePath, false);
  }

  protected DeployableProjectModel getDeployableProjectModel(String deployablePath, boolean includeTestDependencies)
      throws URISyntaxException {
    return new MavenDeployableProjectModelBuilder(getDeployableFolder(deployablePath), false, includeTestDependencies).build();
  }

  protected File getDeployableFolder(String appPath) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(appPath).toURI());
  }

}
