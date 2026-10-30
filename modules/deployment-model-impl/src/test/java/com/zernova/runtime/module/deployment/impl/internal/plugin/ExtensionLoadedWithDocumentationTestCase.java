/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.plugin;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver.defaultClassLoaderResolver;
import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository.getExtensionModelLoaderManager;
import static com.zernova.test.allure.AllureConstants.ExtensionModelDiscoveryFeature.EXTENSION_MODEL_DISCOVERY;

import static java.util.Collections.emptyMap;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionDiscoveryRequest;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelDiscoverer;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.activation.internal.descriptor.DefaultDeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.internal.maven.MavenDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.Test;

@Feature(EXTENSION_MODEL_DISCOVERY)
public class ExtensionLoadedWithDocumentationTestCase extends AbstractZernovaTestCase {

  @Test
  @Issue("W-12289043")
  public void discoverExtensionModelIncludingDocumentation() throws Exception {
    ApplicationDescriptor applicationDescriptor = createApplicationDescriptor("apps/extension-with-doc");
    ZernovaDeployableArtifactClassLoader applicationClassLoader =
        defaultClassLoaderResolver().createApplicationClassLoader(applicationDescriptor);

    ExtensionModelLoaderRepository extensionModelLoaderRepository = getExtensionModelLoaderManager();
    startIfNeeded(extensionModelLoaderRepository);

    Set<ExtensionModel> extensionModels =
        ExtensionModelDiscoverer.defaultExtensionModelDiscoverer(applicationClassLoader, extensionModelLoaderRepository)
            .discoverPluginsExtensionModels(ExtensionDiscoveryRequest.builder()
                .setArtifactPlugins(applicationDescriptor.getPlugins()).build());

    assertThat(extensionModels, hasItem(hasProperty("name", is("Database"))));
    ExtensionModel dbExtensionModel = extensionModels.stream().filter(em -> em.getName().equals("Database")).findAny().get();
    assertThat(dbExtensionModel.getDescription().trim(), not(is("")));

    stopIfNeeded(extensionModelLoaderRepository);
  }

  protected ApplicationDescriptor createApplicationDescriptor(String appPath)
      throws URISyntaxException {
    DeployableArtifactDescriptorFactory deployableArtifactDescriptorFactory =
        new DefaultDeployableArtifactDescriptorFactory();
    DeployableProjectModel model = getDeployableProjectModel(appPath);

    return deployableArtifactDescriptorFactory.createApplicationDescriptor(model, emptyMap());
  }

  protected DeployableProjectModel getDeployableProjectModel(String deployablePath)
      throws URISyntaxException {
    return new MavenDeployableProjectModelBuilder(getDeployableFolder(deployablePath), false, false).build();
  }

  protected File getDeployableFolder(String appPath) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(appPath).toURI());
  }

}
