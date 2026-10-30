/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.javaxinject;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.sdk.api.values.Value;
import com.zernova.sdk.api.values.ValueBuilder;
import com.zernova.sdk.api.values.ValueProvider;
import com.zernova.sdk.api.values.ValueResolvingException;

import java.util.Set;

import javax.inject.Inject;

public class JavaxInjectCompatibilityTestValueProvider implements ValueProvider {

  @Inject
  private ArtifactEncoding encoding;

  public String getId() {
    return "id";
  }

  public Set<Value> resolve() throws ValueResolvingException {
    return ValueBuilder.getValuesFor(encoding.getDefaultEncoding().name());
  }
}
