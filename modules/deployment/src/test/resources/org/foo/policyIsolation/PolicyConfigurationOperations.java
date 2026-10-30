/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.policyIsolation;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;

public class PolicyConfigurationOperations {

  public void checkExplicitConfiguration(@Config PolicyConfigurationExtension config) {
    if (config.getConfigurationParameter().equals(PolicyConfigurationExtension.CONFIG_PARAMETER_DEFAULT_VALUE)) {
      throw new IllegalStateException("Operation explicit config should not contain the default configuration");
    }
  }

  public void checkImplicitConfiguration(@Config PolicyConfigurationExtension config) {
    if (!config.getConfigurationParameter().equals(PolicyConfigurationExtension.CONFIG_PARAMETER_DEFAULT_VALUE)) {
      throw new IllegalStateException("Operation implicit config should contain the default configuration");
    }
  }

}
