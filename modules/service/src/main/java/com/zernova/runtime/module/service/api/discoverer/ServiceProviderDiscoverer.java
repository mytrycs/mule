/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.api.discoverer;


import com.zernova.runtime.api.service.ServiceProvider;

import java.util.List;

/**
 * Discovers the {@link ServiceProvider} available in the container.
 */
public interface ServiceProviderDiscoverer {

  /**
   * Discovers available service assemblies.
   *
   * @return a non null list of {@link ServiceAssembly} found in the container.
   * @throws ServiceResolutionError when a {@link ServiceProvider} cannot be properly instantiated.
   */
  List<ServiceAssembly> discover() throws ServiceResolutionError;
}
