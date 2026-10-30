/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.classloader.container;

import static com.zernova.runtime.container.internal.ContainerClassLoaderCreatorUtils.SYSTEM_PACKAGES;

import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.container.api.ZernovaContainerClassLoaderWrapper;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Test utility that allows to manage the container class loader for Mule functional test cases.
 *
 * @since 4.5
 */
public interface TestContainerClassLoaderAssembler {

  static TestContainerClassLoaderAssembler create(List<String> extraBootPackages, Set<String> extraPrivilegedArtifacts,
                                                  List<URL> muleApisOptUrls,
                                                  List<URL> muleApisUrls,
                                                  List<URL> optUrls,
                                                  List<URL> muleUrls) {
    return new DefaultTestContainerClassLoaderAssembler(extraBootPackages, extraPrivilegedArtifacts, muleApisOptUrls,
                                                        muleApisUrls, optUrls, muleUrls);
  }

  /**
   * @return a collection with the system packages.
   */
  static Collection<String> getSystemPackages() {
    return SYSTEM_PACKAGES;
  }

  /**
   * @return the container class loader with all the configuration needed for the tests.
   */
  ZernovaContainerClassLoaderWrapper createContainerClassLoader();

  /**
   * @return the module repository used for creating the container class loader.
   */
  ModuleRepository getModuleRepository();

}
