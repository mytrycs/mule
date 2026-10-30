/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.value;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

import com.zernova.sdk.api.values.Value;
import com.zernova.sdk.api.values.ValueProvider;
import com.zernova.sdk.api.values.ValueResolvingException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adapter of the legacy {@link com.zernova.runtime.extension.api.values.ValueProvider} to {@link ValueProvider}
 *
 * @since 4.4.0
 */
public class SdkValueProviderAdapter implements ValueProvider {

  private final com.zernova.runtime.extension.api.values.ValueProvider valueProvider;

  public SdkValueProviderAdapter(com.zernova.runtime.extension.api.values.ValueProvider valueProvider) {
    this.valueProvider = valueProvider;
  }

  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    try {
      Set<Value> values = valueProvider.resolve()
          .stream().map(SdkValueAdapter::new)
          .collect(toCollection(LinkedHashSet::new));
      return unmodifiableSet(values);
    } catch (com.zernova.runtime.extension.api.values.ValueResolvingException e) {
      throw new ValueResolvingException(e.getMessage(), e.getFailureCode(), e.getCause());
    }
  }

  @Override
  public String getId() {
    return valueProvider.getId();
  }
}
