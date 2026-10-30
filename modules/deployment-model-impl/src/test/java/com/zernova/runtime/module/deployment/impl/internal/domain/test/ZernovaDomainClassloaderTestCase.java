/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain.test;

import static com.zernova.runtime.core.internal.context.ArtifactStoppedPersistenceListener.ARTIFACT_STOPPED_LISTENER;
import static com.zernova.tck.mockito.answer.BuilderAnswer.BUILDER_ANSWER;
import static com.zernova.tck.util.ZernovaContextUtils.mockMuleContext;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.lifecycle.LifecycleManager;
import com.zernova.runtime.core.internal.context.ArtifactStoppedPersistenceListener;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.domain.DomainDescriptor;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactContextBuilder;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultZernovaDomain;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SmallTest
public class ZernovaDomainClassloaderTestCase extends AbstractZernovaTestCase {

  private static final ArtifactContextBuilder artifactContextBuilder = mock(ArtifactContextBuilder.class, BUILDER_ANSWER);

  @ClassRule
  public static TemporaryFolder artifactInstallationDirectory = new TemporaryFolder();

  private final ClassLoaderRepository domainClassLoaderRepository = mock(ClassLoaderRepository.class);
  private final DomainDescriptor domainDescriptor = mock(DomainDescriptor.class);
  private final ArtifactClassLoader artifactClassLoader = mock(ArtifactClassLoader.class);
  private final ServiceRepository serviceRepository = mock(ServiceRepository.class);
  private final List<ArtifactPlugin> artifactPlugins = emptyList();
  private final ExtensionModelLoaderRepository extensionModelLoaderRepository = mock(ExtensionModelLoaderRepository.class);
  private final ClassLoader originalThreadClassloader = mock(ClassLoader.class);
  private final ClassLoader domainClassloader = mock(ClassLoader.class);
  private final ArtifactContext artifactContext = mock(ArtifactContext.class);
  private final ZernovaContext muleContext = mockMuleContext();
  private final ZernovaRegistry muleRegistry = mock(ZernovaRegistry.class);
  private final ArtifactStoppedPersistenceListener artifactStoppedPersistenceListener =
      mock(ArtifactStoppedPersistenceListener.class);
  private final LifecycleManager lifecycleManager = mock(LifecycleManager.class);
  private ClassLoader classloaderUsedInDispose;
  private Domain domain;

  @Before
  public void setUp() throws Exception {

    domain = new TestZernovaDomain(domainDescriptor, artifactClassLoader, domainClassLoaderRepository, serviceRepository,
                                artifactPlugins, extensionModelLoaderRepository);
    currentThread().setContextClassLoader(originalThreadClassloader);
    when(domainDescriptor.getDeploymentProperties()).thenReturn(empty());
    when(domainDescriptor.getDataFolderName()).thenReturn("dataFolderName");
    when(artifactContextBuilder.build()).thenReturn(artifactContext);
    when(artifactContext.getMuleContext()).thenReturn(muleContext);
    when(muleContext.getLifecycleManager()).thenReturn(lifecycleManager);

    when(lifecycleManager.isDirectTransition(Stoppable.PHASE_NAME)).thenReturn(true);

    domain.init();
    when(artifactClassLoader.getClassLoader()).thenReturn(domainClassloader);
  }

  @Test
  public void disposeWithDomainClassloader() {
    doAnswer(invocation -> {
      classloaderUsedInDispose = currentThread().getContextClassLoader();
      return null;
    }).when(muleContext).dispose();

    doAnswer(invocation -> artifactStoppedPersistenceListener).when(muleRegistry).lookupObject(ARTIFACT_STOPPED_LISTENER);

    domain.dispose();

    assertThat(classloaderUsedInDispose, sameInstance(domainClassloader));
    assertThat(currentThread().getContextClassLoader(), is(originalThreadClassloader));
  }

  @Test
  public void stopWithDomainClassloader() throws Exception {
    doAnswer(invocation -> {
      classloaderUsedInDispose = currentThread().getContextClassLoader();
      return null;
    }).when(muleContext).stop();

    doAnswer(invocation -> artifactStoppedPersistenceListener).when(muleRegistry).lookupObject(ARTIFACT_STOPPED_LISTENER);

    domain.stop();

    assertThat(classloaderUsedInDispose, sameInstance(domainClassloader));
    assertThat(currentThread().getContextClassLoader(), is(originalThreadClassloader));
  }

  private static final class TestZernovaDomain extends DefaultZernovaDomain {

    public TestZernovaDomain(DomainDescriptor descriptor, ArtifactClassLoader deploymentClassLoader,
                          ClassLoaderRepository classLoaderRepository, ServiceRepository serviceRepository,
                          List<ArtifactPlugin> artifactPlugins, ExtensionModelLoaderRepository extensionModelLoaderRepository) {
      super(descriptor, deploymentClassLoader, classLoaderRepository, serviceRepository, artifactPlugins,
            extensionModelLoaderRepository, mock(MemoryManagementService.class),
            mock(ArtifactConfigurationProcessor.class));
    }

    @Override
    protected ArtifactContextBuilder getArtifactContextBuilder() {
      return artifactContextBuilder;
    }

    @Override
    protected File getArtifactInstallationDirectory() {
      return ZernovaDomainClassloaderTestCase.artifactInstallationDirectory.getRoot();
    }
  }

}
