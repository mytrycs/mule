/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.service.el;

import static com.zernova.runtime.api.el.ValidationResult.success;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionExecutionException;
import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.el.ValidationResult;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;

import java.util.Iterator;

public class MockExpressionLanguage implements ExpressionLanguage {

  public void addGlobalBindings(BindingContext bindingContext) {
    //Do nothing
  }

  public TypedValue<?> evaluate(String expression, BindingContext context) throws ExpressionExecutionException {
    throw new UnsupportedOperationException();
  }

  public TypedValue<?> evaluate(String expression, DataType expectedOutputType, BindingContext context)
    throws ExpressionExecutionException {
    throw new UnsupportedOperationException();
  }

  public TypedValue<?> evaluateLogExpression(String expression, BindingContext context) throws ExpressionExecutionException {
    throw new UnsupportedOperationException();
  }

  public ValidationResult validate(String expression) {
    return success();
  }

  public Iterator<TypedValue<?>> split(String expression, BindingContext context) {
    throw new UnsupportedOperationException();
  }

}
