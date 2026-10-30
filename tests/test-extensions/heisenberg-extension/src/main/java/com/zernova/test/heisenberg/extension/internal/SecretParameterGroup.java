/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.internal;

import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.sdk.api.annotation.param.Parameter;

/**
 * A non-api ParameterGroup object meant to test {@link ComponentParameterization} use cases involving complex objects not exposed
 * by the extension classloader
 */
public class SecretParameterGroup {

  @Parameter
  private String secret;

  public String getSecret() {
    return secret;
  }
}
