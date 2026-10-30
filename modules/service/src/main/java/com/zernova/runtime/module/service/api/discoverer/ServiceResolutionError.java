/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.api.discoverer;

import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.service.Service;

/**
 * Thrown to indicate an error during the resolution process of {@link Service} instances.
 */
@NoInstantiate
public final class ServiceResolutionError extends Exception {

  /**
   * @inherited
   */
  public ServiceResolutionError(String message) {
    super(message);
  }

  /**
   * @inherited
   */
  public ServiceResolutionError(String message, Throwable cause) {
    super(message, cause);
  }
}
