/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.extension.api.annotation.metadata.RequiredForMetadata;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.semantics.security.Password;
import com.zernova.sdk.api.annotation.semantics.security.Username;

public class MetadataConnectionProvider implements ConnectionProvider<MetadataConnection>, Startable {

  public static boolean STARTED = false;

  @Parameter
  @Optional
  @RequiredForMetadata
  @Username
  String user;

  @Parameter
  @Optional
  @Password
  String password;

  @Override
  public MetadataConnection connect() throws ConnectionException {
    return new MetadataConnection();
  }

  @Override
  public void disconnect(MetadataConnection metadataConnection) {

  }

  @Override
  public ConnectionValidationResult validate(MetadataConnection metadataConnection) {
    return ConnectionValidationResult.success();
  }

  @Override
  public void start() throws ZernovaException {
    STARTED = true;
  }
}
