/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.attribute;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * Delegate evaluator contract for configuration attributes.
 *
 * @since 4.2.0
 */
public interface AttributeEvaluatorDelegate<T> {

  /**
   * Resolves the attribute's value.
   *
   * @param event   the current event being processed.
   * @param manager the expression manager used to evaluate the values.
   * @return a new resolved {@link TypedValue}.
   */
  TypedValue<T> resolve(CoreEvent event, ExtendedExpressionManager manager);

  /**
   * Resolves the attribute's value using a pre-constructed session that caches a set of bindings.
   *
   * @param session the expression manager used to evaluate the values.
   * @return a new resolved {@link TypedValue}.
   */
  TypedValue<T> resolve(ExpressionManagerSession session);

  /**
   * Resolves the attribute's value using a {@link BindingContext}, for compatibility purposes.
   *
   * @return a new resolved {@link TypedValue}.
   * @deprecated added for compatibility purposes, use {@link #resolve(ExpressionManagerSession)}.
   */
  TypedValue<T> resolve(BindingContext context, ExtendedExpressionManager expressionManager);
}
