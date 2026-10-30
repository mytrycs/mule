/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.connectivity.basic;

import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.api.meta.ExpressionSupport;

@Extension(name = "Basic")
@Operations(VoidOperations.class)
@ConnectionProviders(TestConnectionProvider.class)
public class TestConnector {

  @Parameter
  private Owner requiredPojoDefault;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private Owner requiredPojoNoExpression;

  @Parameter
  @Expression(ExpressionSupport.REQUIRED)
  private Owner requiredPojoExpressionRequired;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  private Owner requiredPojoExpressionSupported;

  @Parameter
  @Optional
  private Owner optionalPojoDefault;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Optional
  private Owner optionalPojoNoExpression;

  @Parameter
  @Expression(ExpressionSupport.REQUIRED)
  @Optional
  private Owner optionalPojoExpressionRequired;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional
  private Owner optionalPojoExpressionSupported;

}
