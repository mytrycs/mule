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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adapter of {@link com.zernova.runtime.api.value.Value} to {@link com.zernova.sdk.api.values.Value}
 *
 * @since 4.4.0
 */
public class SdkValueAdapter implements Value {

  private final com.zernova.runtime.api.value.Value value;

  public SdkValueAdapter(com.zernova.runtime.api.value.Value value) {
    this.value = value;
  }

  @Override
  public String getId() {
    return value.getId();
  }

  @Override
  public String getDisplayName() {
    return value.getDisplayName();
  }

  @Override
  public Set<Value> getChilds() {
    Set<Value> values = value.getChilds()
        .stream().map(SdkValueAdapter::new)
        .collect(toCollection(LinkedHashSet::new));
    return unmodifiableSet(values);
  }

  @Override
  public String getPartName() {
    return value.getPartName();
  }
}
