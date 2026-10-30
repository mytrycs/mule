/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.attribute;

import static com.zernova.runtime.api.metadata.DataType.STRING;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * {@link AttributeEvaluatorDelegate} that resolves attribute values with multiple expressions in a given string.
 *
 * @since 4.2.0
 */
public final class ParseAttributeEvaluatorDelegate implements AttributeEvaluatorDelegate<String> {

  private String attributeValue;

  public ParseAttributeEvaluatorDelegate(String attributeValue) {
    this.attributeValue = attributeValue;
  }

  @Override
  public TypedValue<String> resolve(CoreEvent event, ExtendedExpressionManager expressionManager) {
    return new TypedValue<>(expressionManager.parse(attributeValue, event, null), STRING);
  }

  @Override
  public TypedValue<String> resolve(BindingContext context, ExtendedExpressionManager expressionManager) {
    throw buildNoEventException();
  }

  @Override
  public TypedValue<String> resolve(ExpressionManagerSession session) {
    throw buildNoEventException();
  }

  private UnsupportedOperationException buildNoEventException() {
    return new UnsupportedOperationException("Cannot use a PARSE_EXPRESSION attribute type without an event.");
  }
}
