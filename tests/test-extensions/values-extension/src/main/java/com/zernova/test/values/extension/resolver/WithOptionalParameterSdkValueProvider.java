/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.resolver;


import static com.zernova.sdk.api.values.ValueBuilder.getValuesFor;

import java.util.Set;

import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.values.Value;
import com.zernova.sdk.api.values.ValueProvider;

public class WithOptionalParameterSdkValueProvider implements ValueProvider {

  @Optional
  @Parameter
  String optionalValue;

  @Override
  public Set<Value> resolve() {
    return getValuesFor(optionalValue == null ? "Optional value ommited" : optionalValue);
  }

  @Override
  public String getId() {
    return getClass().getName();
  }
}
