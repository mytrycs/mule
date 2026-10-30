/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.zernova.runtime.container.api.CoreExtensionsAware;
import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.container.api.ZernovaCoreExtensionDependency;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.registry.IllegalDependencyInjectionException;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

@SmallTest
public class ReflectionZernovaCoreExtensionDependencyResolverTestCase extends AbstractZernovaTestCase {

  public static final String EXTENSION1 = "extension1";
  public static final String EXTENSION2 = "extension2";
  public static final String DEPENDANT_EXTENSION = "dependantCoreExtension";

  private ZernovaCoreExtensionDependencyDiscoverer dependencyDiscoverer = mock(ZernovaCoreExtensionDependencyDiscoverer.class);
  private ReflectionZernovaCoreExtensionDependencyResolver dependencyResolver =
      new ReflectionZernovaCoreExtensionDependencyResolver(dependencyDiscoverer);

  @Test
  public void resolvesEmptyDependencies() throws Exception {
    List<ZernovaCoreExtension> coreExtensions = Collections.EMPTY_LIST;
    dependencyResolver.resolveDependencies(coreExtensions);
    verify(dependencyDiscoverer, times(0)).findDependencies(any());
  }

  @Test
  public void resolvesIndependentExtensions() throws Exception {
    ZernovaCoreExtension extension1 = mockCoreExtension(EXTENSION1);
    ZernovaCoreExtension extension2 = mockCoreExtension(EXTENSION2);

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(extension2);
    coreExtensions.add(extension1);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);
    verify(dependencyDiscoverer, times(2)).findDependencies(any());

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), sameInstance(extension1));
    assertThat(resolvedCoreExtensions.get(1), sameInstance(extension2));
  }

  @Test
  public void resolvesDependentExtensionWhenDependencyIsCreatedFirst() throws Exception {
    TestCoreExtension testCoreExtension = new TestCoreExtension();
    TestCoreExtensionDependency dependantCoreExtension = mock(TestCoreExtensionDependency.class);
    when(dependantCoreExtension.getName()).thenReturn(DEPENDANT_EXTENSION);

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(testCoreExtension);
    coreExtensions.add(dependantCoreExtension);

    final List<LinkedZernovaCoreExtensionDependency> dependencies = new LinkedList<>();
    final LinkedZernovaCoreExtensionDependency linkedMuleCoreExtensionDependency =
        new LinkedZernovaCoreExtensionDependency(TestCoreExtension.class, TestCoreExtensionDependency.class
            .getMethod("setTestCoreExtension", TestCoreExtension.class));
    dependencies.add(linkedMuleCoreExtensionDependency);

    when(dependencyDiscoverer.findDependencies(dependantCoreExtension)).thenReturn(dependencies);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);

    verify(dependantCoreExtension).setTestCoreExtension(testCoreExtension);

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), is(instanceOf(TestCoreExtension.class)));
    assertThat(resolvedCoreExtensions.get(1), is(instanceOf(TestCoreExtensionDependency.class)));
  }

  @Test
  public void resolvesDependentExtensionWhenDependantIsCreatedFirst() throws Exception {
    TestCoreExtension testCoreExtension = new TestCoreExtension();
    TestCoreExtensionDependency dependantCoreExtension = mock(TestCoreExtensionDependency.class);
    when(dependantCoreExtension.getName()).thenReturn(DEPENDANT_EXTENSION);

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(dependantCoreExtension);
    coreExtensions.add(testCoreExtension);

    final List<LinkedZernovaCoreExtensionDependency> dependencies = new LinkedList<>();
    final LinkedZernovaCoreExtensionDependency linkedMuleCoreExtensionDependency =
        new LinkedZernovaCoreExtensionDependency(TestCoreExtension.class, TestCoreExtensionDependency.class
            .getMethod("setTestCoreExtension", TestCoreExtension.class));
    dependencies.add(linkedMuleCoreExtensionDependency);

    when(dependencyDiscoverer.findDependencies(dependantCoreExtension)).thenReturn(dependencies);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);

    verify(dependantCoreExtension).setTestCoreExtension(testCoreExtension);

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), is(instanceOf(TestCoreExtension.class)));
    assertThat(resolvedCoreExtensions.get(1), is(instanceOf(TestCoreExtensionDependency.class)));
  }

  @Test
  public void resolvesCoreExtensionAwareAndStandardExtensions() throws Exception {
    ZernovaCoreExtension coreExtensionsAwareExtension = mockCoreExtension(EXTENSION1);
    ZernovaCoreExtension testCoreExtension = new TestCoreExtension();

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(testCoreExtension);
    coreExtensions.add(coreExtensionsAwareExtension);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), sameInstance(testCoreExtension));
    assertThat(resolvedCoreExtensions.get(1), sameInstance(coreExtensionsAwareExtension));
  }

  @Test
  public void resolvesStandardAndCoreExtensionAwareExtensions() throws Exception {
    ZernovaCoreExtension coreExtensionsAwareExtension = mockCoreExtension("coreExtensionsAwareExtension");
    ZernovaCoreExtension testCoreExtension = new TestCoreExtension();

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(coreExtensionsAwareExtension);
    coreExtensions.add(testCoreExtension);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), sameInstance(testCoreExtension));
    assertThat(resolvedCoreExtensions.get(1), sameInstance(coreExtensionsAwareExtension));
  }

  @Test
  public void resolvesOrderedCoreExtensionAwareExtensions() throws Exception {
    ZernovaCoreExtension coreExtensionsAwareExtension1 = mockCoreExtension(EXTENSION1);
    ZernovaCoreExtension coreExtensionsAwareExtension2 = mockCoreExtension(EXTENSION2);

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(coreExtensionsAwareExtension1);
    coreExtensions.add(coreExtensionsAwareExtension2);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), sameInstance(coreExtensionsAwareExtension1));
    assertThat(resolvedCoreExtensions.get(1), sameInstance(coreExtensionsAwareExtension2));
  }

  @Test
  public void resolvesDisorderedCoreExtensionAwareExtensions() throws Exception {
    ZernovaCoreExtension coreExtensionsAwareExtension1 = mockCoreExtension(EXTENSION1);
    ZernovaCoreExtension coreExtensionsAwareExtension2 = mockCoreExtension(EXTENSION2);

    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    coreExtensions.add(coreExtensionsAwareExtension2);
    coreExtensions.add(coreExtensionsAwareExtension1);

    final List<ZernovaCoreExtension> resolvedCoreExtensions = dependencyResolver.resolveDependencies(coreExtensions);

    assertThat(resolvedCoreExtensions.size(), equalTo(2));
    assertThat(resolvedCoreExtensions.get(0), sameInstance(coreExtensionsAwareExtension1));
    assertThat(resolvedCoreExtensions.get(1), sameInstance(coreExtensionsAwareExtension2));
  }

  @Test(expected = UnresolveableDependencyException.class)
  public void throwsExceptionOnUnresolvedDependency() throws Exception {
    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    TestCoreExtensionDependency dependantCoreExtension = mock(TestCoreExtensionDependency.class);
    coreExtensions.add(dependantCoreExtension);

    final List<LinkedZernovaCoreExtensionDependency> dependencies = new LinkedList<>();
    final LinkedZernovaCoreExtensionDependency linkedMuleCoreExtensionDependency =
        new LinkedZernovaCoreExtensionDependency(TestCoreExtension.class, TestCoreExtensionDependency.class
            .getMethod("setTestCoreExtension", TestCoreExtension.class));
    dependencies.add(linkedMuleCoreExtensionDependency);
    when(dependencyDiscoverer.findDependencies(dependantCoreExtension)).thenReturn(dependencies);

    dependencyResolver.resolveDependencies(coreExtensions);
  }

  @Test(expected = IllegalDependencyInjectionException.class)
  public void throwsExceptionOnIllegalDependency() throws Exception {
    List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
    TestIllegalCoreExtensionDependency dependantCoreExtension = mock(TestIllegalCoreExtensionDependency.class);
    coreExtensions.add(dependantCoreExtension);

    final List<LinkedZernovaCoreExtensionDependency> dependencies = new LinkedList<>();
    final LinkedZernovaCoreExtensionDependency linkedMuleCoreExtensionDependency =
        new LinkedZernovaCoreExtensionDependency(TestCoreExtension.class, TestCoreExtensionDependency.class
            .getMethod("setTestCoreExtension", TestCoreExtension.class));
    dependencies.add(linkedMuleCoreExtensionDependency);
    when(dependencyDiscoverer.findDependencies(dependantCoreExtension)).thenReturn(dependencies);

    dependencyResolver.resolveDependencies(coreExtensions);
  }

  private ZernovaCoreExtension mockCoreExtension(String extension11) {
    ZernovaCoreExtension extension1 = mock(CoreExtensionsAwareExtension.class);
    when(extension1.getName()).thenReturn(extension11);

    return extension1;
  }

  public static class AbstractTestCoreExtension implements ZernovaCoreExtension {

    @Override
    public void dispose() {}

    @Override
    public void initialise() throws InitialisationException {}

    @Override
    public String getName() {
      return null;
    }

    @Override
    public void start() throws ZernovaException {}

    @Override
    public void stop() throws ZernovaException {}

    @Override
    public void setContainerClassLoader(ArtifactClassLoader containerClassLoader) {

    }
  }

  public static class TestCoreExtension extends AbstractTestCoreExtension {

    @Override
    public String getName() {
      return "testCoreExtension";
    }
  }

  public interface TestCoreExtensionDependency extends ZernovaCoreExtension {

    @ZernovaCoreExtensionDependency
    void setTestCoreExtension(TestCoreExtension coreExtension);
  }

  public interface TestIllegalCoreExtensionDependency extends ZernovaCoreExtension, CoreExtensionsAware {

    @ZernovaCoreExtensionDependency
    void setTestCoreExtension(TestCoreExtension coreExtension);
  }

  public interface CoreExtensionsAwareExtension extends ZernovaCoreExtension, CoreExtensionsAware {

  }
}
