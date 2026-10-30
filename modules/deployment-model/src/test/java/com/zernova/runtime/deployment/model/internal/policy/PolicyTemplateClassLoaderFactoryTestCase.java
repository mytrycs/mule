/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.policy;

import static com.zernova.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SmallTest
public class PolicyTemplateClassLoaderFactoryTestCase extends AbstractZernovaTestCase {

  private static final String POLICY_ID = "policy/policyId";

  private final ClassLoaderLookupPolicy lookupPolicy = mock(ClassLoaderLookupPolicy.class);
  @Rule
  public TemporaryFolder policyFolder = new TemporaryFolder();
  private final PolicyTemplateClassLoaderFactory factory = new PolicyTemplateClassLoaderFactory();
  private PolicyTemplateDescriptor descriptor;
  private RegionClassLoader regionClassLoader;
  private ArtifactClassLoader parentClassLoader;

  @Before
  public void setUp() throws Exception {
    descriptor = new PolicyTemplateDescriptor("testPolicy");
    descriptor.setRootFolder(policyFolder.getRoot());

    regionClassLoader = mock(RegionClassLoader.class);
    when(regionClassLoader.getClassLoader()).thenReturn(getClass().getClassLoader());

    parentClassLoader = mock(ArtifactClassLoader.class);
    when(parentClassLoader.getClassLoader()).thenReturn(regionClassLoader);
    when(lookupPolicy.getClassLookupStrategy(anyString())).thenReturn(PARENT_FIRST);
    when(parentClassLoader.getClassLoaderLookupPolicy()).thenReturn(lookupPolicy);
  }

  @Test
  public void createsEmptyClassLoader() throws Exception {
    final ArtifactClassLoader artifactClassLoader = factory.create(POLICY_ID, parentClassLoader, descriptor);
    final ZernovaArtifactClassLoader classLoader = (ZernovaArtifactClassLoader) artifactClassLoader.getClassLoader();
    assertThat(classLoader.getURLs(), equalTo(new URL[0]));
  }

  @Test(expected = IllegalArgumentException.class)
  public void validatesPolicyFolder() throws Exception {
    File fakePolicyFolder = new File("./fake/folder/for/test");
    descriptor.setRootFolder(fakePolicyFolder);
    factory.create(POLICY_ID, null, descriptor);
  }

  @Test
  public void usesClassLoaderLookupPolicy() throws Exception {
    final ArtifactClassLoader artifactClassLoader = factory.create(POLICY_ID, parentClassLoader, descriptor);
    final ZernovaArtifactClassLoader classLoader = (ZernovaArtifactClassLoader) artifactClassLoader.getClassLoader();

    final String className = "com.dummy.Foo";
    when(regionClassLoader.loadClass(className)).thenThrow(ClassNotFoundException.class);
    try {
      classLoader.loadClass(className);
      fail("Able to load an un-existent class");
    } catch (ClassNotFoundException e) {
      // Expected
    }

    verify(lookupPolicy).getClassLookupStrategy(className);
  }

  @Test
  public void createsClassLoaderWithPlugins() throws Exception {
    ArtifactClassLoader pluginClassLoader1 = mock(ArtifactClassLoader.class);
    ArtifactClassLoader pluginClassLoader2 = mock(ArtifactClassLoader.class);
    List<ArtifactClassLoader> artifactPluginClassLoaders = new ArrayList<>();
    artifactPluginClassLoaders.add(pluginClassLoader1);
    artifactPluginClassLoaders.add(pluginClassLoader2);

    when(regionClassLoader.getArtifactPluginClassLoaders()).thenReturn(artifactPluginClassLoaders);

    final ArtifactClassLoader artifactClassLoader = factory.create(POLICY_ID, parentClassLoader, descriptor);
    final ZernovaDeployableArtifactClassLoader classLoader =
        (ZernovaDeployableArtifactClassLoader) artifactClassLoader.getClassLoader();

    assertThat(classLoader.getArtifactPluginClassLoaders(), contains(pluginClassLoader1, pluginClassLoader2));
  }
}
