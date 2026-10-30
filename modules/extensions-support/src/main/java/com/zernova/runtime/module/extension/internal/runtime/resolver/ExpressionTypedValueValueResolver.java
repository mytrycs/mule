/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.core.api.util.ClassUtils.isInstance;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.transformation.TransformationService;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import jakarta.inject.Inject;

/**
 * A {@link ValueResolver} implementation and extension of {@link TypeSafeExpressionValueResolver } which evaluates expressions
 * and tries to ensure that the output is always of a certain type.
 * <p>
 * This {@link ValueResolver} will return the {@link TypedValue} of the expression evaluation result.
 *
 * @param <T>
 * @since 4.0
 */
public class ExpressionTypedValueValueResolver<T> extends ExpressionValueResolver<TypedValue<T>> implements Initialisable {

  private final Class<T> expectedClass;
  private TypeSafeTransformer typeSafeTransformer;

  @Inject
  private TransformationService transformationService;

  public ExpressionTypedValueValueResolver(String expression, Class<T> expectedClass) {
    super(expression, DataType.fromType(expectedClass));
    this.expectedClass = expectedClass;
  }

  @Override
  public TypedValue<T> resolve(ValueResolvingContext context) throws ZernovaException {
    TypedValue<T> typedValue = resolveTypedValue(context);
    if (!isInstance(expectedClass, typedValue.getValue())) {
      DataType expectedDataType =
          DataType.builder()
              .type(expectedClass)
              .mediaType(typedValue.getDataType().getMediaType())
              .build();
      return new TypedValue<>(typeSafeTransformer.<T>transform(typedValue.getValue(), typedValue.getDataType(), expectedDataType),
                              expectedDataType);
    }
    return typedValue;
  }

  public void setTransformationService(TransformationService transformationService) {
    this.transformationService = transformationService;
  }

  @Override
  public void initialise() throws InitialisationException {
    super.initialise();
    this.typeSafeTransformer = new TypeSafeTransformer(transformationService);
  }
}
