/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.hello;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;

public class PrivilegedOperation {

  public PrivilegedOperation() {}

  @MediaType(value = TEXT_PLAIN, strict = false)
  public String printMessage(@Config PrivilegedExtension config) {
    System.out.println("Test privileged extension says: " + config.getMessage());
    return config.getMessage();
  }
}
