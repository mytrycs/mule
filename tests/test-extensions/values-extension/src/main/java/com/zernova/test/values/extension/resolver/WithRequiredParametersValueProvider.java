/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.resolver;

import static com.zernova.runtime.extension.api.values.ValueBuilder.getValuesFor;
import com.zernova.runtime.api.value.Value;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.values.ValueProvider;
import com.zernova.sdk.api.annotation.param.Optional;

import java.util.List;
import java.util.Set;

public class WithRequiredParametersValueProvider implements ValueProvider {

  @Optional
  @Parameter
  String requiredString;

  @Parameter
  int requiredInteger;

  @Parameter
  boolean requiredBoolean;

  @Parameter
  List<String> strings;

  @Override
  public Set<Value> resolve() {
    return getValuesFor("requiredString:" + requiredString, "requiredInteger:" + requiredInteger,
                        "requiredBoolean:" + requiredBoolean, "strings:" + strings);
  }
}
