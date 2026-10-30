/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.some.extension;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.failure;
import static com.zernova.test.heisenberg.extension.HeisenbergErrors.HEALTH;
import static com.zernova.test.heisenberg.extension.HeisenbergErrors.OAUTH2;
import com.zernova.runtime.api.connection.CachedConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.exception.ModuleException;

public class ExtConnProvider implements CachedConnectionProvider<String> {

  private static final ModuleException OAUTH_MODULE_EXCEPTION = new ModuleException(OAUTH2, new RuntimeException());
  private static final ModuleException HEALTH_MODULE_EXCEPTION = new ModuleException(HEALTH, new RuntimeException());
  private static final CustomConnectionException DOMAIN_HEALTH_CONNECTION_EXCEPTION =
      new CustomConnectionException(HEALTH_MODULE_EXCEPTION);
  private static final CustomConnectionException DOMAIN_OAUTH_CONNECTION_EXCEPTION =
      new CustomConnectionException(OAUTH_MODULE_EXCEPTION);
  private static final ConnectionException CONNECTION_EXCEPTION = new ConnectionException("Some Error", HEALTH_MODULE_EXCEPTION);

  @Parameter
  @Optional(defaultValue = "false")
  public boolean fail;

  @Parameter
  @Optional(defaultValue = "false")
  public boolean domainException;

  @Override
  public String connect() throws ConnectionException {
    if (fail) {
      throw domainException
          ? DOMAIN_HEALTH_CONNECTION_EXCEPTION
          : CONNECTION_EXCEPTION;
    }
    return "";
  }

  @Override
  public void disconnect(String connection) {

  }

  @Override
  public ConnectionValidationResult validate(String connection) {
    return domainException
        ? failure("This is a failure", DOMAIN_OAUTH_CONNECTION_EXCEPTION)
        : failure("This is a failure", OAUTH_MODULE_EXCEPTION);
  }


}
