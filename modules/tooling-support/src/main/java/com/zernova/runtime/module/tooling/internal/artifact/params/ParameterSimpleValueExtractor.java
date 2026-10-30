/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.artifact.params;

import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.app.declaration.api.ParameterValue;
import com.zernova.runtime.app.declaration.api.ParameterValueVisitor;
import com.zernova.runtime.app.declaration.api.fluent.ParameterListValue;
import com.zernova.runtime.app.declaration.api.fluent.ParameterObjectValue;
import com.zernova.runtime.app.declaration.api.fluent.ParameterSimpleValue;

/**
 * {@link ParameterModel} simple value extractor. Only accepts parameters of type {@ParameterSimpleValue} otherwise an exception
 * is thrown.
 */
public class ParameterSimpleValueExtractor implements ParameterValueVisitor {

  private String value;

  public static String extractSimpleValue(ParameterValue parameterValue) {
    ParameterSimpleValueExtractor extractor = new ParameterSimpleValueExtractor();
    parameterValue.accept(extractor);
    return extractor.get();
  }

  private String get() {
    return value;
  }

  @Override
  public void visitSimpleValue(ParameterSimpleValue text) {
    this.value = text.getValue();
  }

  @Override
  public void visitListValue(ParameterListValue list) {
    throw new IllegalStateException("Only simple values expected");
  }

  @Override
  public void visitObjectValue(ParameterObjectValue objectValue) {
    throw new IllegalStateException("Only simple values expected");
  }
}
