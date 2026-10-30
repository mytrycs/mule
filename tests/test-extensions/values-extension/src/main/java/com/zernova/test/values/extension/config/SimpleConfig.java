/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.config;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.values.extension.ValuesOperations;
import com.zernova.test.values.extension.connection.ConnectionWithFailureErrorProvider;
import com.zernova.test.values.extension.connection.ConnectionWithValueFourBoundActingParameters;
import com.zernova.test.values.extension.connection.ConnectionWithParameterWithFieldValues;
import com.zernova.test.values.extension.connection.ConnectionWithValueParameter;
import com.zernova.test.values.extension.connection.ConnectionWithValueWithRequiredParam;
import com.zernova.test.values.extension.connection.ConnectionWithValuesWithRequiredParamsFromParamGroup;
import com.zernova.test.values.extension.connection.ConnectionWithValuesWithRequiredParamsFromShowInDslGroup;
import com.zernova.test.values.extension.connection.ValuesConnectionProvider;
import com.zernova.test.values.extension.source.SimpleSourceWithParameterWithFieldValues;
import com.zernova.test.values.extension.source.SourceMustNotStart;
import com.zernova.test.values.extension.source.SourceWithConfiguration;
import com.zernova.test.values.extension.source.SourceWithConnection;
import com.zernova.test.values.extension.source.SourceWithMultiLevelFieldValues;
import com.zernova.test.values.extension.source.SourceWithMultiLevelValue;
import com.zernova.test.values.extension.source.SourceWithRequiredParameterInsideShowInDslGroup;
import com.zernova.test.values.extension.source.SourceWithRequiredParameterWithAlias;
import com.zernova.test.values.extension.source.SourceWithValuesWithRequiredParameterInsideParamGroup;
import com.zernova.test.values.extension.source.SourceWithValuesWithRequiredParameters;

import java.util.List;

@Configuration
@ConnectionProviders({ValuesConnectionProvider.class, ConnectionWithValueParameter.class,
    ConnectionWithValueWithRequiredParam.class, ConnectionWithValuesWithRequiredParamsFromParamGroup.class,
    ConnectionWithValuesWithRequiredParamsFromShowInDslGroup.class, ConnectionWithFailureErrorProvider.class,
    ConnectionWithValueFourBoundActingParameters.class, ConnectionWithParameterWithFieldValues.class})
@Operations({ValuesOperations.class})
@Sources({SourceWithConfiguration.class, SourceWithConnection.class, SourceWithValuesWithRequiredParameters.class,
    SourceWithValuesWithRequiredParameterInsideParamGroup.class})
@com.zernova.sdk.api.annotation.Sources({SourceWithRequiredParameterWithAlias.class,
    SourceWithRequiredParameterInsideShowInDslGroup.class,
    SourceWithMultiLevelValue.class, SourceMustNotStart.class, SimpleSourceWithParameterWithFieldValues.class,
    SourceWithMultiLevelFieldValues.class})
public class SimpleConfig {

  @Parameter
  @Optional(defaultValue = "noExpression")
  private String data;

  @Parameter
  @Optional
  private List<String> configValues;

  public String getData() {
    return data;
  }

  public List<String> getConfigValues() {
    return configValues;
  }
}
