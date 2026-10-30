/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.api.annotation.NoInstantiate;

import java.net.URL;

/**
 * Defines a service that will be exported by a module to other Mule artifacts via SPI.
 *
 * @since 4.0
 */
@NoInstantiate
public final class ExportedService {

  private final String serviceInterface;
  private final URL resource;

  /**
   * Create a new service
   *
   * @param serviceInterface fully qualified name of the interface that defines the service to be located using SPI. Non empty.
   * @param resource         resource to be returned when {code serviceInterface} is searched via SPI. Non null.
   */
  public ExportedService(String serviceInterface, URL resource) {
    checkArgument(!isEmpty(serviceInterface), "serviceInterface cannot be empty");
    checkArgument(resource != null, "resource cannot be null");

    this.serviceInterface = serviceInterface;
    this.resource = resource;
  }

  public String getServiceInterface() {
    return serviceInterface;
  }

  public URL getResource() {
    return resource;
  }
}
