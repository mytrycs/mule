/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.source;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.binding.Binding;
import com.zernova.test.values.extension.resolver.WithTwoActingParametersValueProvider;

@MediaType(TEXT_PLAIN)
public class SourceWithTwoBoundActingParameters extends AbstractSource {

  @Parameter
  @com.zernova.sdk.api.annotation.values.OfValues(
      value = WithTwoActingParametersValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue", extractionExpression = "oneParameter"),
          @Binding(actingParameter = "scalarActingParameter", extractionExpression = "someParameter")})
  private String parameterWithValue;

  @Parameter
  private String oneParameter;

  @Parameter
  private String someParameter;


}
