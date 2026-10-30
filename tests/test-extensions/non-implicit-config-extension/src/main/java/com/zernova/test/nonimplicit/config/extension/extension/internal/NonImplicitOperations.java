/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.nonimplicit.config.extension.extension.internal;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.test.nonimplicit.config.extension.extension.api.Counter;
import com.zernova.test.nonimplicit.config.extension.extension.api.NonImplicitConfigExtension;

public class NonImplicitOperations {

  public NonImplicitConfigExtension getConfig(@com.zernova.sdk.api.annotation.param.Config NonImplicitConfigExtension config) {
    return config;
  }

  public Counter getConnection(@Connection Counter connection) {
    return connection;
  }
}
