/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.classloader.container;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import static java.util.Collections.emptyList;

import com.zernova.runtime.container.api.ZernovaModule;
import com.zernova.runtime.container.api.discoverer.ModuleDiscoverer;
import com.zernova.runtime.container.internal.DefaultModuleRepository;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link ModuleDiscoverer} that enables to change discovered modules to enable testing privileged API scenarios.
 *
 * @since 4.5
 */
public class TestModuleDiscoverer implements ModuleDiscoverer {

  private final Set<String> privilegedArtifactIds;
  private final ModuleDiscoverer delegateModuleDiscoverer;

  /**
   * Creates a module discoverer
   *
   * @param privilegedArtifactIds identifiers of the artifacts that will be conceded privileged API access. Non null
   */
  public TestModuleDiscoverer(Set<String> privilegedArtifactIds, ModuleDiscoverer moduleDiscoverer) {
    checkArgument(privilegedArtifactIds != null, "privilegedArtifactIds cannot be null");
    checkArgument(moduleDiscoverer != null, "moduleDiscoverer cannot be null");

    this.privilegedArtifactIds = privilegedArtifactIds;
    this.delegateModuleDiscoverer = moduleDiscoverer;
  }

  @Override
  public List<ZernovaContainerModule> discover() {
    DefaultModuleRepository containerModuleDiscoverer =
        new DefaultModuleRepository(delegateModuleDiscoverer);

    List<ZernovaContainerModule> discoveredModules = containerModuleDiscoverer.getModules();
    List<ZernovaContainerModule> updateModules = new ArrayList<>(discoveredModules.size());
    for (ZernovaContainerModule discoveredModule : discoveredModules) {
      if (!discoveredModule.getPrivilegedExportedPackages().isEmpty()) {
        discoveredModule = updateModuleForTests(discoveredModule);
      }
      updateModules.add(discoveredModule);
    }

    return updateModules;
  }

  private ZernovaContainerModule updateModuleForTests(ZernovaContainerModule discoveredModule) {
    Set<String> privilegedArtifacts = new HashSet<>(discoveredModule.getPrivilegedArtifacts());
    privilegedArtifacts.addAll(privilegedArtifactIds);

    return new ZernovaModule(discoveredModule.getName(), discoveredModule.getExportedPackages(),
                          discoveredModule.getExportedPaths(), discoveredModule.getPrivilegedExportedPackages(),
                          privilegedArtifacts,
                          discoveredModule instanceof ZernovaModule
                              ? ((ZernovaModule) discoveredModule).getExportedServices()
                              : emptyList());
  }
}
