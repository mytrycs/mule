/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.artifact;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXTENSION_MANAGER;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.extension.internal.manager.CompositeArtifactExtensionManager;
import com.zernova.runtime.module.extension.api.manager.ExtensionManagerFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class CompositeArtifactExtensionManagerFactoryTestCase extends AbstractZernovaTestCase {

  @Test
  public void createsCompositeExtensionManager() throws Exception {

    Application application = mock(Application.class, RETURNS_DEEP_STUBS);

    final Registry registry = mock(Registry.class);
    when(application.getRegistry()).thenReturn(registry);
    final ArtifactContext artifactContext = mock(ArtifactContext.class);
    when(artifactContext.getRegistry()).thenReturn(registry);
    when(application.getArtifactContext()).thenReturn(artifactContext);

    ExtensionManager applicationExtensionManager = mock(ExtensionManager.class);
    when(registry.lookupByName(OBJECT_EXTENSION_MANAGER))
        .thenReturn(of(applicationExtensionManager));

    ExtensionModelLoaderRepository extensionModelLoaderRepository = mock(ExtensionModelLoaderRepository.class);

    ExtensionManagerFactory extensionManagerFactory = mock(ExtensionManagerFactory.class);
    CompositeArtifactExtensionManagerFactory factory = new CompositeArtifactExtensionManagerFactory(application,
                                                                                                    extensionModelLoaderRepository,
                                                                                                    emptyList(),
                                                                                                    extensionManagerFactory);

    ExtensionManager policyExtensionManager = mock(ExtensionManager.class);
    ZernovaContext muleContext = mock(ZernovaContext.class);
    when(extensionManagerFactory.create(muleContext)).thenReturn(policyExtensionManager);

    ExtensionManager extensionManager = factory.create(muleContext);

    assertThat(extensionManager, instanceOf(CompositeArtifactExtensionManager.class));
    CompositeArtifactExtensionManager compositeArtifactExtensionManager = (CompositeArtifactExtensionManager) extensionManager;
    assertThat(compositeArtifactExtensionManager.getParentExtensionManager(), equalTo(applicationExtensionManager));
    assertThat(compositeArtifactExtensionManager.getChildExtensionManager(), equalTo(policyExtensionManager));
  }
}
