/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.container.api.ZernovaCoreExtensionDependency;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.List;

import org.junit.Test;

@SmallTest
public class ReflectionZernovaCoreExtensionDependencyDiscovererTestCase extends AbstractZernovaTestCase {

  private ReflectionZernovaCoreExtensionDependencyDiscoverer dependencyDiscoverer =
      new ReflectionZernovaCoreExtensionDependencyDiscoverer();

  @Test
  public void resolvesEmptyDependencies() throws Exception {
    final List<LinkedZernovaCoreExtensionDependency> dependencies = dependencyDiscoverer.findDependencies(new TestCoreExtension());
    assertThat(dependencies.size(), equalTo(0));
  }

  @Test
  public void resolvesSingleDependency() throws Exception {
    final List<LinkedZernovaCoreExtensionDependency> dependencies =
        dependencyDiscoverer.findDependencies(new DependantTestCoreExtension());
    assertThat(dependencies.size(), equalTo(1));
    assertThat((Class<TestCoreExtension>) dependencies.get(0).getDependencyClass(), equalTo(TestCoreExtension.class));
    assertThat(dependencies.get(0).getDependantMethod().getName(), equalTo("setTestCoreExtension"));
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

  }

  public static class DependantTestCoreExtension extends AbstractTestCoreExtension {

    @ZernovaCoreExtensionDependency
    public void setTestCoreExtension(TestCoreExtension coreExtension) {

    }
  }
}
