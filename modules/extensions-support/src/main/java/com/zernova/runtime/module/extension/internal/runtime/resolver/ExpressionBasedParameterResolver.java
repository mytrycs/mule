/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import java.util.Optional;

/**
 * {@link ParameterResolver} implementation for the parameters that are resolved from an expression
 *
 * @param <T> Concrete parameter type to be resolved
 * @since 4.0
 */
class ExpressionBasedParameterResolver<T> implements ParameterResolver<T> {

  private final String expression;
  private final ValueResolvingContext context;
  private final TypeSafeExpressionValueResolver<T> valueResolver;

  ExpressionBasedParameterResolver(String expression,
                                   TypeSafeExpressionValueResolver<T> valueResolver,
                                   ValueResolvingContext context) {
    this.expression = expression;
    this.valueResolver = valueResolver;
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T resolve() {
    try {
      return valueResolver.resolve(context);
    } catch (ZernovaException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getExpression() {
    return ofNullable(expression);
  }
}
