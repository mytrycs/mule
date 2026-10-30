/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_POLICY_ISOLATION_PROPERTY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXTENSION_MANAGER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.POLICY;
import static com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration.NULL_CLASSLOADER_CONFIGURATION;
import static com.zernova.runtime.module.deployment.impl.internal.policy.PolicyExtensionManagerFactory.HTTP_EXTENSION_NAME;
import static com.zernova.runtime.module.deployment.impl.internal.policy.PolicyExtensionManagerFactory.SOCKETS_EXTENSION_NAME;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;
import static com.zernova.test.allure.AllureConstants.ArtifactDeploymentFeature.POLICY_DEPLOYMENT;
import static com.zernova.test.allure.AllureConstants.ArtifactDeploymentFeature.DeploymentSuccessfulStory.POLICY_ISOLATION;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.policy.PolicyParametrization;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplate;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionDiscoveryRequest;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelDiscoverer;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.policy.api.PolicyPointcut;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.mockito.ArgumentCaptor;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(POLICY_DEPLOYMENT)
@Story(POLICY_ISOLATION)
@RunWith(Parameterized.class)
public class DefaultApplicationPolicyInstanceTestCase extends AbstractZernovaTestCase {

  @Parameters(name = "enablePolicyIsolation: {0}")
  public static Boolean[] parameters() {
    return new Boolean[] {false, true};
  }

  public boolean enablePolicyIsolation;

  @Rule
  public SystemProperty enablePolicyIsolationRule;

  private final ServiceRepository serviceRepository = mock(ServiceRepository.class);

  private PolicyTemplate policyTemplate;
  private Application application;
  private Registry appRegistry;

  private ZernovaContext policyMuleContext;

  public DefaultApplicationPolicyInstanceTestCase(boolean enablePolicyIsolationPropertyValue) {
    this.enablePolicyIsolation = enablePolicyIsolationPropertyValue;
    this.enablePolicyIsolationRule =
        new SystemProperty(ENABLE_POLICY_ISOLATION_PROPERTY, Boolean.toString(enablePolicyIsolationPropertyValue));
  }

  @Before
  public void setUp() throws RegistrationException {
    policyTemplate = mock(PolicyTemplate.class, RETURNS_DEEP_STUBS);
    when(policyTemplate.getArtifactClassLoader().getClassLoader()).thenReturn(this.getClass().getClassLoader());
    when(policyTemplate.getDescriptor()).thenReturn(new PolicyTemplateDescriptor("policyId"));

    application = mock(Application.class, RETURNS_DEEP_STUBS);

    ArtifactContext appArtifactContext = application.getArtifactContext();
    ZernovaContext appMuleContext = mockContextWithServices();
    when(appArtifactContext.getMuleContext()).thenReturn(appMuleContext);

    appRegistry = appArtifactContext.getRegistry();
    doReturn(of(appMuleContext)).when(appRegistry).lookupByType(ZernovaContext.class);

    doReturn(appRegistry).when(application).getRegistry();

    policyMuleContext = mockContextWithServices();
  }

  @Test
  @Issue("MULE-14289")
  public void correctArtifactTypeForPolicies() {
    ZernovaContextListener muleContextListener = mock(ZernovaContextListener.class);
    ArgumentCaptor<ZernovaContext> muleContextCaptor = ArgumentCaptor.forClass(ZernovaContext.class);

    doReturn(of(mock(ExtensionManager.class))).when(appRegistry).lookupByName(OBJECT_EXTENSION_MANAGER);

    PolicyParametrization parameterization =
        new PolicyParametrization("policyId", mock(PolicyPointcut.class), 1, emptyMap(), mock(File.class), emptyList());
    DefaultApplicationPolicyInstance applicationPolicyInstance =
        new DefaultApplicationPolicyInstance(application, policyTemplate,
                                             parameterization,
                                             serviceRepository,
                                             mock(ClassLoaderRepository.class),
                                             mock(ExtensionModelLoaderRepository.class),
                                             muleContextListener,
                                             mock(ArtifactConfigurationProcessor.class));

    try {
      applicationPolicyInstance.initialise();
    } catch (InitialisationException e) {
      // Initialization is expected to fail because of missing context
      // But the data to do the assertion on must already have been called
    }

    verify(muleContextListener).onCreation(muleContextCaptor.capture());
    assertThat(muleContextCaptor.getValue().getArtifactType(), is(POLICY));
  }

  @Test
  @Issue("W-11233266")
  public void policyWithHttpOnAppWithHttpUsesHttpFromTheApp() {
    // add http and sockets as dependencies in the app...
    ExtensionManager appExtensionManager = mock(ExtensionManager.class);
    ExtensionModel appHttpExtModel = mockExtensionModel(HTTP_EXTENSION_NAME);
    ExtensionModel appSocketsExtModel = mockExtensionModel(SOCKETS_EXTENSION_NAME);
    doReturn(of(appHttpExtModel)).when(appExtensionManager).getExtension(HTTP_EXTENSION_NAME);
    doReturn(of(appSocketsExtModel)).when(appExtensionManager).getExtension(SOCKETS_EXTENSION_NAME);
    doReturn(new HashSet<>(asList(appHttpExtModel, appSocketsExtModel))).when(appExtensionManager)
        .getExtensions();

    doReturn(of(appExtensionManager)).when(appRegistry).lookupByName(OBJECT_EXTENSION_MANAGER);

    // add http and sockets as plugin dependencies declared in the policies...
    doReturn(asList(mockArtifactPlugin(HTTP_EXTENSION_NAME), mockArtifactPlugin(SOCKETS_EXTENSION_NAME)))
        .when(policyTemplate).getOwnArtifactPlugins();

    ExtensionModelDiscoverer extModelDiscoverer = mock(ExtensionModelDiscoverer.class);

    PolicyExtensionManagerFactory policyExtensionManagerFactory =
        new PolicyExtensionManagerFactory(application, policyTemplate, mock(ExtensionModelLoaderRepository.class),
                                          enablePolicyIsolation, (pcl, eml) -> extModelDiscoverer);

    policyExtensionManagerFactory.create(policyMuleContext);

    ArgumentCaptor<ExtensionDiscoveryRequest> extDiscoveryRequestCaptor = forClass(ExtensionDiscoveryRequest.class);
    verify(extModelDiscoverer).discoverPluginsExtensionModels(extDiscoveryRequestCaptor.capture());

    List<String> policyExtensionNames = extDiscoveryRequestCaptor.getValue().getArtifactPluginDescriptors().stream()
        .map(ArtifactPluginDescriptor::getName)
        .collect(toList());

    if (enablePolicyIsolation) {
      // ... the policy has itself http and sockets because it doesn't use the ones form the app
      assertThat(policyExtensionNames.toString(), policyExtensionNames,
                 hasItems(HTTP_EXTENSION_NAME, SOCKETS_EXTENSION_NAME));
    } else {
      // ... the policy does not have itself http or sockets because it uses the ones form the app
      assertThat(policyExtensionNames.toString(), policyExtensionNames,
                 not(hasItems(HTTP_EXTENSION_NAME, SOCKETS_EXTENSION_NAME)));
    }
  }

  @Test
  @Issue("W-11233266")
  public void policyWithHttpOnAppWithoutHttpUsesHttpFromThePolicy() {
    // do NOT add http as dependencies in the app...
    ExtensionManager appExtensionManager = mock(ExtensionManager.class);
    ExtensionModel appSocketsExtModel = mockExtensionModel(SOCKETS_EXTENSION_NAME);
    doReturn(of(appSocketsExtModel)).when(appExtensionManager).getExtension(SOCKETS_EXTENSION_NAME);
    doReturn(singleton(appSocketsExtModel)).when(appExtensionManager).getExtensions();

    doReturn(of(appExtensionManager)).when(appRegistry).lookupByName(OBJECT_EXTENSION_MANAGER);

    // add http and sockets as plugin dependencies declared in the policies...
    doReturn(asList(mockArtifactPlugin(HTTP_EXTENSION_NAME), mockArtifactPlugin(SOCKETS_EXTENSION_NAME)))
        .when(policyTemplate).getOwnArtifactPlugins();

    ExtensionModelDiscoverer extModelDiscoverer = mock(ExtensionModelDiscoverer.class);

    PolicyExtensionManagerFactory policyExtensionManagerFactory =
        new PolicyExtensionManagerFactory(application, policyTemplate, mock(ExtensionModelLoaderRepository.class),
                                          enablePolicyIsolation, (pcl, eml) -> extModelDiscoverer);

    policyExtensionManagerFactory.create(policyMuleContext);

    ArgumentCaptor<ExtensionDiscoveryRequest> extDiscoveryRequestCaptor = forClass(ExtensionDiscoveryRequest.class);
    verify(extModelDiscoverer).discoverPluginsExtensionModels(extDiscoveryRequestCaptor.capture());

    List<String> policyExtensionNames = extDiscoveryRequestCaptor.getValue().getArtifactPluginDescriptors().stream()
        .map(ArtifactPluginDescriptor::getName)
        .collect(toList());

    if (enablePolicyIsolation) {
      // ... the policy has itself http and sockets because it doesn't use the ones form the app
      assertThat(policyExtensionNames.toString(), policyExtensionNames,
                 hasItems(HTTP_EXTENSION_NAME, SOCKETS_EXTENSION_NAME));
    } else {
      // ... the policy has itself http but not sockets
      assertThat(policyExtensionNames.toString(), policyExtensionNames,
                 allOf(hasItem(HTTP_EXTENSION_NAME), not(hasItem(SOCKETS_EXTENSION_NAME))));
    }
  }

  private ArtifactPlugin mockArtifactPlugin(String name) {
    ArtifactPluginDescriptor artifactPluginDescriptor = mock(ArtifactPluginDescriptor.class);
    when(artifactPluginDescriptor.getName()).thenReturn(name);
    when(artifactPluginDescriptor.getBundleDescriptor()).thenReturn(new BundleDescriptor.Builder()
        .setGroupId("mule")
        .setArtifactId(name)
        .setVersion("1.0.0")
        .build());
    when(artifactPluginDescriptor.getClassLoaderConfiguration()).thenReturn(NULL_CLASSLOADER_CONFIGURATION);

    ArtifactPlugin artifactPlugin = mock(ArtifactPlugin.class);
    when(artifactPlugin.getDescriptor()).thenReturn(artifactPluginDescriptor);
    when(artifactPlugin.getArtifactClassLoader()).thenReturn(mock(ArtifactClassLoader.class));

    return artifactPlugin;
  }

  private ExtensionModel mockExtensionModel(String name) {
    ExtensionModel extModel = mock(ExtensionModel.class);
    when(extModel.getName()).thenReturn(name);

    return extModel;
  }
}
