/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.runtime.parameter.Literal;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

/**
 * {@link ValueResolver} that produces instances of {@link Literal}
 *
 * @param <T> the generic type of the value represented by the literal
 * @since 4.0
 */
public class StaticLiteralValueResolver<T> implements ValueResolver<Literal<T>> {

  private final Literal<T> literal;

  /**
   * Creates a new instance
   *
   * @param value the literal value
   * @param type  the type of the value represented by the literal
   */
  public StaticLiteralValueResolver(String value, Class<T> type) {
    literal = new ImmutableLiteral<>(value, type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Literal<T> resolve(ValueResolvingContext context) throws ZernovaException {
    return literal;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isDynamic() {
    return false;
  }
}
