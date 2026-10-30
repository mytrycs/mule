/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.connection;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.binding.Binding;
import com.zernova.test.values.extension.resolver.WithFourActingParametersValueProvider;

@Alias("withValueFourBoundActingParameters")
public class ConnectionWithValueFourBoundActingParameters extends AbstractConnectionProvider {

  @Parameter
  @com.zernova.sdk.api.annotation.values.OfValues(
      value = WithFourActingParametersValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue", extractionExpression = "parameterOne"),
          @Binding(actingParameter = "anotherValue", extractionExpression = "parameterTwo"),
          @Binding(actingParameter = "someValue", extractionExpression = "parameterThree"),
          @Binding(actingParameter = "optionalValue", extractionExpression = "parameterFour")})
  String parameterWithValue;

  @Parameter
  String parameterOne;

  @Parameter
  String parameterTwo;

  @Parameter
  String parameterThree;

  @Parameter
  String parameterFour;
}
