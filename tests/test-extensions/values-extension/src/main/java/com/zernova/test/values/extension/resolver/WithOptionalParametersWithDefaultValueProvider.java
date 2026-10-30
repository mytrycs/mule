/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.resolver;

import com.zernova.runtime.api.value.Value;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.values.ValueProvider;

import java.util.Set;

import static java.util.Collections.emptySet;
import org.apache.logging.log4j.util.Strings;
import static com.zernova.runtime.extension.api.values.ValueBuilder.getValuesFor;

public class WithOptionalParametersWithDefaultValueProvider implements ValueProvider {

  @Parameter
  @Optional(defaultValue = "VP_DEFAULT_VALUE")
  String optionalValue;

  @Override
  public Set<Value> resolve() {
    if (Strings.isBlank(optionalValue)) {
      return emptySet();
    }
    return getValuesFor("optionalValue:" + optionalValue);
  }
}
