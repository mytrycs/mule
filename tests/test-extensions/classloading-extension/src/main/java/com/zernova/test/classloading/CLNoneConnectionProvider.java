/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.classloading;

import static com.zernova.test.classloading.api.ClassLoadingHelper.addClassLoader;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;

@Alias("none")
public class CLNoneConnectionProvider implements ClassLoadingConnectionProvider<String> {

  public static final String CONNECT = "Connect";
  public static final String DISCONNECT = "Disconnect";

  @Override
  public String connect() throws ConnectionException {
    addClassLoader(CONNECT + getKind());
    return "Conn";
  }

  @Override
  public void disconnect(String connection) {
    addClassLoader(DISCONNECT + getKind());
  }

  @Override
  public ConnectionValidationResult validate(String connection) {
    addClassLoader("Validate" + getKind());
    return ConnectionValidationResult.success();
  }

  public String getKind() {
    return "NONE";
  }
}
