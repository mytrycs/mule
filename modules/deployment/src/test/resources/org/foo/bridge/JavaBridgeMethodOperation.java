/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.bridge;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

/**
 * Operation for testing purposes
 */
public class JavaBridgeMethodOperation  {

  public JavaBridgeMethodOperation() {
  }

  @MediaType(value = TEXT_PLAIN, strict = false)
  public String printMessage(@Config JavaBridgeMethodExtension config) {
    return config.getMessage();
  }
}
