/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.isInstance;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.internal.util.attribute.AttributeEvaluator;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import jakarta.inject.Inject;

/**
 * A {@link ValueResolver} which evaluates a MEL expressions
 * <p>
 * It resolves the expressions by making use of the {@link AttributeEvaluator} so that it's compatible with simple expressions and
 * templates alike
 *
 * @param <T>
 * @since 4.0
 */
public class ExpressionValueResolver<T> implements ExpressionBasedValueResolver<T>, Initialisable {

  @Inject
  private ExtendedExpressionManager extendedExpressionManager;

  final AttributeEvaluator evaluator;
  private final String expression;

  public ExpressionValueResolver(String expression, DataType expectedDataType) {
    checkArgument(!isBlank(expression), "Expression cannot be blank or null");
    this.expression = expression;
    this.evaluator = new AttributeEvaluator(expression, expectedDataType);
  }

  public ExpressionValueResolver(String expression) {
    checkArgument(!isBlank(expression), "Expression cannot be blank or null");
    this.expression = expression;
    this.evaluator = new AttributeEvaluator(expression);

  }

  void setExtendedExpressionManager(ExtendedExpressionManager extendedExpressionManager) {
    this.extendedExpressionManager = extendedExpressionManager;
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(extendedExpressionManager);
    getEvaluator().initialize(extendedExpressionManager);
  }

  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    TypedValue<T> typedValue = resolveTypedValue(context);

    Object value = typedValue.getValue();

    if (isInstance(ValueResolver.class, value)) {
      value = ((ValueResolver) value).resolve(context);
    }

    return (T) value;
  }

  protected <V> TypedValue<V> resolveTypedValue(ValueResolvingContext context) {
    if (context.getSession() != null) {
      return evaluator.resolveTypedValue(context.getSession());
    } else {
      return evaluator.resolveTypedValue(context.getEvent());
    }
  }

  /**
   * @return {@code true}
   */
  @Override
  public boolean isDynamic() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExpression() {
    return expression;
  }

  private AttributeEvaluator getEvaluator() {
    return evaluator;
  }
}
