/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process;

public class TestUtils {

  public static final String DEBUG_MODE_SYSTEM_PROPERTY_KEY = "debug";

  public static int getTimeout(int timeout) {
    if (System.getProperty(DEBUG_MODE_SYSTEM_PROPERTY_KEY) != null) {
      return 999999;
    }
    return timeout;
  }
}
