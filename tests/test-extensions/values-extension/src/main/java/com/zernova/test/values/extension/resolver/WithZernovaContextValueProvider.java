/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.resolver;

import com.zernova.runtime.extension.api.values.ValueBuilder;
import com.zernova.runtime.api.value.Value;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.extension.api.values.ValueProvider;

import java.util.Set;

public class WithZernovaContextValueProvider implements ValueProvider, ZernovaContextAware {

  private ZernovaContext context;

  @Override
  public Set<Value> resolve() {
    if (context != null) {
      return ValueBuilder.getValuesFor("INJECTED!!!");
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.context = context;
  }
}
