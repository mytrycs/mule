/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.connectivity.basic;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.api.meta.ExpressionSupport;

public class TestConnectionProvider implements ConnectionProvider<Object> {

  @Parameter
  private String connectionProviderRequiredFieldDefault;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  private String connectionProviderRequiredFieldExpressionSupported;


  @Parameter
  @Expression(ExpressionSupport.REQUIRED)
  private String connectionProviderRequiredFieldExpressionRequired;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String connectionProviderRequiredFieldExpressionNotSupported;

  @Parameter
  @Optional
  private String connectionProviderOptionalFieldDefault;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional
  private String connectionProviderOptionalFieldExpressionSupported;

  @Parameter
  @Expression(ExpressionSupport.REQUIRED)
  @Optional
  private String connectionProviderOptionalFieldExpressionRequired;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Optional
  private String connectionProviderOptionalFieldExpressionNotSupported;

  @Override
  public Object connect() throws ConnectionException {
    return new Object();
  }

  @Override
  public void disconnect(Object o) {

  }

  @Override
  public ConnectionValidationResult validate(Object o) {
    return ConnectionValidationResult.success();
  }
}
