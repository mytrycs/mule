/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.transformation.TransformationService;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.internal.util.attribute.AttributeEvaluator;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import jakarta.inject.Inject;

/**
 * A {@link ValueResolver} which evaluates expressions and tries to ensure that the output is always of a certain type.
 * <p>
 * If the expression does not return a value of that type, then it tries to locate a {@link Transformer} which can do the
 * transformation from the obtained type to the expected one.
 * <p>
 * It resolves the expressions by making use of the {@link AttributeEvaluator} so that it's compatible with simple expressions and
 * templates alike
 *
 * @param <T>
 * @since 3.7.0
 */
public class TypeSafeExpressionValueResolver<T> implements ValueResolver<T>, Initialisable {

  private final Class<T> expectedType;
  private final String expression;
  private TypeSafeValueResolverWrapper<T> delegate;

  @Inject
  private TransformationService transformationService;

  @Inject
  private ExtendedExpressionManager extendedExpressionManager;

  private final DataType expectedDataType;

  public TypeSafeExpressionValueResolver(String expression, Class<T> expectedType, DataType expectedDataType) {
    checkArgument(expectedType != null, "expected type cannot be null");
    this.expression = expression;
    this.expectedType = expectedType;
    this.expectedDataType = expectedDataType;
  }

  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    return delegate.resolve(context);
  }

  /**
   * @return {@code true}
   */
  @Override
  public boolean isDynamic() {
    return true;
  }

  @Override
  public void initialise() throws InitialisationException {
    ExpressionValueResolver resolver = new ExpressionValueResolver(expression, expectedDataType);
    resolver.setExtendedExpressionManager(extendedExpressionManager);
    resolver.initialise();

    delegate = new TypeSafeValueResolverWrapper<>(resolver, expectedType);
    delegate.setTransformationService(transformationService);

    delegate.initialise();
  }

  public void setTransformationService(TransformationService transformationService) {
    this.transformationService = transformationService;
  }

  public void setExtendedExpressionManager(ExtendedExpressionManager extendedExpressionManager) {
    this.extendedExpressionManager = extendedExpressionManager;
  }

}
