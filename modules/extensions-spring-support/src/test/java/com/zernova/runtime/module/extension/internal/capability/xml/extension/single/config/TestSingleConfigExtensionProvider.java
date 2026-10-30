/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.extension.single.config;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;

/**
 * Provider description
 */
public class TestSingleConfigExtensionProvider implements ConnectionProvider<String> {

  /**
   * Connection Param Description
   */
  @Parameter
  private String connectionParam1;

  @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "Connection Group")
  private SingleConfigParameterGroup group;

  @Override
  public String connect() throws ConnectionException {
    return "";
  }

  @Override
  public void disconnect(String connection) {

  }

  @Override
  public ConnectionValidationResult validate(String connection) {
    return null;
  }
}
