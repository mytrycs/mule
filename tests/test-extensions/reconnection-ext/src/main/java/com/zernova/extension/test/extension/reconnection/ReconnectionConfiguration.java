/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.connectivity.ConnectionProviders;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple operations since
 * they represent something core from the extension.
 */
@ConnectionProviders(ReconnectableConnectionProvider.class)
@Operations(ReconnectionOperations.class)
@Sources({ReconectionSource.class})
@com.zernova.sdk.api.annotation.Sources({FallibleReconnectableSource.class, NonReconnectableSource.class})
@Configuration(name = "config")
public class ReconnectionConfiguration {

  @Parameter
  private String configId;

  public String getConfigId() {
    return configId;
  }
}
