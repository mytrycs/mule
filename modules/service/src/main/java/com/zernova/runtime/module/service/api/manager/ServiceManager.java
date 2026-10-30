/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.api.manager;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.module.service.api.discoverer.ServiceDiscoverer;
import com.zernova.runtime.module.service.internal.manager.ZernovaServiceManager;

/**
 * Manages the lifecycle of the services available in the {@link ServiceRepository}
 */
@NoImplement
public interface ServiceManager extends Startable, Stoppable, ServiceRepository {

  static ServiceManager create(ServiceDiscoverer serviceDiscoverer) {
    return new ZernovaServiceManager(serviceDiscoverer);
  }
}
