/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static com.zernova.runtime.container.internal.ContainerModulesDiscovererProvider.containerModulesDiscoverer;

import com.zernova.runtime.container.api.discoverer.ModuleDiscoverer;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Discovers modules on the Mule container.
 *
 * @since 4.0
 */
public final class ContainerModuleDiscoverer implements ModuleDiscoverer {

  private final List<ModuleDiscoverer> moduleDiscoverers;

  /**
   * Creates a new instance.
   *
   * @param containerClassLoader container classloader used to find modules. Non null.
   */
  public ContainerModuleDiscoverer() {
    this.moduleDiscoverers = getModuleDiscoverers();
  }

  private List<ModuleDiscoverer> getModuleDiscoverers() {
    List<ModuleDiscoverer> result = new ArrayList<>();
    result.add(new JreModuleDiscoverer());
    result.add(containerModulesDiscoverer());
    return result;
  }

  public void addModuleDiscoverer(ModuleDiscoverer moduleDiscoverer) {
    this.moduleDiscoverers.add(moduleDiscoverer);
  }

  @Override
  public List<ZernovaContainerModule> discover() {
    return new CompositeModuleDiscoverer(this.moduleDiscoverers.toArray(new ModuleDiscoverer[0])).discover();
  }
}
