/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.attribute;

import static com.zernova.runtime.api.metadata.DataType.OBJECT;
import static com.zernova.runtime.api.metadata.DataType.STRING;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * {@link AttributeEvaluatorDelegate} that resolves attribute values that are static and wont change during execution.
 *
 * @since 4.2.0
 */
public final class StaticAttributeEvaluatorDelegate implements AttributeEvaluatorDelegate<Object> {

  private final TypedValue<Object> value;

  public StaticAttributeEvaluatorDelegate(String attributeValue) {
    this.value = new TypedValue<>(attributeValue, attributeValue == null ? OBJECT : STRING);
  }

  @Override
  public TypedValue<Object> resolve(CoreEvent event, ExtendedExpressionManager manager) {
    return value;
  }

  @Override
  public TypedValue<Object> resolve(ExpressionManagerSession session) {
    return value;
  }

  @Override
  public TypedValue<Object> resolve(BindingContext context, ExtendedExpressionManager expressionManager) {
    return value;
  }
}
