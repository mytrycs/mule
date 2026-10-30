/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.service;

/**
 * Provides a {@link ServiceDefinition}
 *
 * @since 1.2
 */
public interface ServiceProvider {

  /**
   * Provides a service instance.
   * <p/>
   * A service provider can return different service definitions depending on the execution environment.
   *
   * @return the service definition provided by this instance. Non null.
   */
  ServiceDefinition getServiceDefinition();
}
