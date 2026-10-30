/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.resolver;

import static java.util.Collections.singleton;
import static com.zernova.sdk.api.values.ValueBuilder.newValue;

import com.zernova.runtime.extension.api.values.ValueResolvingException;
import com.zernova.sdk.api.values.Value;
import com.zernova.sdk.api.values.ValueProvider;
import com.zernova.test.values.extension.source.SourceMustNotStart;

import java.util.Set;

public class TrueFalseValueProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    return singleton(newValue(SourceMustNotStart.isStarted ? "TRUE" : "FALSE").build());
  }

  @Override
  public String getId() {
    return "TrueFalseValueProvider-id";
  }
}
