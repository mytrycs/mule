/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.config;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.values.OfValues;
import com.zernova.test.values.extension.connection.ValuesConnectionProvider;
import com.zernova.test.values.extension.resolver.WithRequiredParametersFromConfigValueProvider;

@Configuration(name = "ValueWithRequiredParam")
@ConnectionProviders(ValuesConnectionProvider.class)
public class ConfigWithValueAndRequiredParam {

  @Parameter
  @OfValues(WithRequiredParametersFromConfigValueProvider.class)
  String channel;

  @Parameter
  String required1;

  @Parameter
  String required2;
}
