/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.transformation.TransformationService;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import jakarta.inject.Inject;

/**
 * {@link ValueResolver} implementation for {@link ParameterResolver} that are resolved from an expression
 *
 * @since 4.0
 */
public class ExpressionBasedParameterResolverValueResolver<T> implements ExpressionBasedValueResolver<ParameterResolver<T>>,
    Initialisable {

  private final String expression;
  private final DataType expectedDataType;

  @Inject
  private TransformationService transformationService;

  @Inject
  private ExtendedExpressionManager extendedExpressionManager;

  private final Class<T> type;
  private TypeSafeExpressionValueResolver<T> delegateResolver;

  public ExpressionBasedParameterResolverValueResolver(String expression, Class<T> type, DataType expectedDataType) {
    this.expression = expression;
    this.type = type;
    this.expectedDataType = expectedDataType;
  }

  @Override
  public void initialise() throws InitialisationException {
    delegateResolver = new TypeSafeExpressionValueResolver<>(expression, type, expectedDataType);
    delegateResolver.setExtendedExpressionManager(extendedExpressionManager);
    delegateResolver.setTransformationService(transformationService);

    delegateResolver.initialise();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ParameterResolver<T> resolve(ValueResolvingContext context) throws ZernovaException {
    return new ExpressionBasedParameterResolver<>(expression, delegateResolver, context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return true;
  }

  public void setTransformationService(TransformationService transformationService) {
    this.transformationService = transformationService;
  }

  public void setExtendedExpressionManager(ExtendedExpressionManager extendedExpressionManager) {
    this.extendedExpressionManager = extendedExpressionManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExpression() {
    return expression;
  }
}
