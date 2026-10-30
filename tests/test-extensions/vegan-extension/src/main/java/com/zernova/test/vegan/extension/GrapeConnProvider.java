/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

@Alias("grape-connection")
public class GrapeConnProvider implements ConnectionProvider<VeganPolicy> {

  @NullSafe
  @Parameter
  @Optional
  public VeganPolicy veganPolicy;

  @Override
  public VeganPolicy connect() throws ConnectionException {
    return veganPolicy;
  }

  @Override
  public void disconnect(VeganPolicy connection) {

  }

  @Override
  public ConnectionValidationResult validate(VeganPolicy connection) {
    return success();
  }
}
