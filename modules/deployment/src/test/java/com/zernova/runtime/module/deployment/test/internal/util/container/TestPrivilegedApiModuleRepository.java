/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal.util.container;

import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.container.internal.ClasspathModuleDiscoverer;
import com.zernova.runtime.container.internal.ContainerModuleDiscoverer;
import com.zernova.runtime.container.internal.DefaultModuleRepository;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.test.runner.classloader.container.TestModuleDiscoverer;

import java.util.List;
import java.util.Set;

/**
 * A {@link ModuleRepository} that enables to change discovered modules to enable testing privileged API scenarios.
 *
 * @since 4.5
 */
public final class TestPrivilegedApiModuleRepository implements ModuleRepository {

  private static final String TEST_MODULE_PROPERTIES = "META-INF/mule-test-module.properties";

  private final DefaultModuleRepository moduleRepository;

  public TestPrivilegedApiModuleRepository(Set<String> privligedArtifactIds) {
    final ContainerModuleDiscoverer moduleDiscoverer = new ContainerModuleDiscoverer();
    moduleDiscoverer.addModuleDiscoverer(new ClasspathModuleDiscoverer(TEST_MODULE_PROPERTIES));
    moduleRepository = new DefaultModuleRepository(new TestModuleDiscoverer(privligedArtifactIds, moduleDiscoverer));
  }

  @Override
  public List<ZernovaContainerModule> getModules() {
    return moduleRepository.getModules();
  }
}
