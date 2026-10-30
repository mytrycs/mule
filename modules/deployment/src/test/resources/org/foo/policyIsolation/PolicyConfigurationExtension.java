/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.policyIsolation;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.lang.String;

/**
 * Extension designed to test the policy classloading and context isolation
 * by declaring a configuration in order to test the implicit and explicit config resolutions.
 */
@Extension(name = "policyConfiguration")
@Operations({PolicyConfigurationOperations.class})
public class PolicyConfigurationExtension {

  public static final String CONFIG_PARAMETER_DEFAULT_VALUE = "default value";

  @Parameter
  @Optional(defaultValue = PolicyConfigurationExtension.CONFIG_PARAMETER_DEFAULT_VALUE)
  private String configurationParameter;

  public String getConfigurationParameter() {
    return configurationParameter;
  }

  public void setConfigurationParameter(String configurationParameter) {
    this.configurationParameter = configurationParameter;
  }
}
