/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.complex.config.properties.deprecated.extension.connection;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.connectivity.NoConnectivityTest;

import java.util.List;
import java.util.Map;

@Alias("provider")
public class ComplexTypesConnectionProvider implements ConnectionProvider<PropertiesRepositoryConnection>, NoConnectivityTest {

  @Parameter
  private SomePojo nestedPojo;

  @Parameter
  private List<String> listedTexts;

  @Parameter
  private List<SomePojo> listedPojos;

  @Parameter
  private Map<String, SomePojo> mappedPojos;

  @Override
  public PropertiesRepositoryConnection connect() throws ConnectionException {
    return new PropertiesRepositoryConnection();
  }

  @Override
  public void disconnect(PropertiesRepositoryConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(PropertiesRepositoryConnection connection) {
    return success();
  }

}
