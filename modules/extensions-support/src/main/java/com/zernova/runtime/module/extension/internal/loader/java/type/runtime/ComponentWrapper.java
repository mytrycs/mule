/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.runtime;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.extension.api.annotation.ExpressionFunctions;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.module.extension.api.loader.java.type.ComponentElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ConnectionProviderElement;
import com.zernova.runtime.module.extension.api.loader.java.type.FunctionContainerElement;
import com.zernova.runtime.module.extension.api.loader.java.type.OperationContainerElement;
import com.zernova.runtime.module.extension.api.loader.java.type.SourceElement;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Abstract implementation of {@link ComponentWrapper}
 *
 * @since 4.0
 */
abstract class ComponentWrapper extends TypeWrapper implements ComponentElement {

  ComponentWrapper(Class<?> aClass, ClassTypeLoader typeLoader) {
    super(aClass, typeLoader);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SourceElement> getSources() {
    return concat(
                  collectElements(Sources.class, Sources::value),
                  collectElements(com.zernova.sdk.api.annotation.Sources.class, com.zernova.sdk.api.annotation.Sources::value))
                      .map(s -> new SourceTypeWrapper(s, typeLoader))
                      .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<OperationContainerElement> getOperationContainers() {
    return getOperationClassStream()
        .map(aClass -> new OperationContainerWrapper(aClass, typeLoader))
        .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<FunctionContainerElement> getFunctionContainers() {
    return getExpressionFunctionClassStream()
        .map(c -> new FunctionContainerWrapper(c, typeLoader))
        .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ConnectionProviderElement> getConnectionProviders() {
    return concat(
                  collectElements(ConnectionProviders.class, ConnectionProviders::value),
                  collectElements(com.zernova.sdk.api.annotation.connectivity.ConnectionProviders.class,
                                  com.zernova.sdk.api.annotation.connectivity.ConnectionProviders::value))
                                      .map(c -> new ConnectionProviderTypeWrapper(c, typeLoader))
                                      .collect(toList());
  }

  protected <A extends Annotation> Stream<Class> collectElements(Class<A> annotationClass,
                                                                 Function<A, Class[]> extractTypeFunction) {
    return getAnnotation(annotationClass)
        .map(a -> Stream.of(extractTypeFunction.apply(a)))
        .orElse(Stream.empty());
  }

  protected Stream<Class> getOperationClassStream() {
    return concat(
                  collectElements(Operations.class, Operations::value),
                  collectElements(com.zernova.sdk.api.annotation.Operations.class, com.zernova.sdk.api.annotation.Operations::value));
  }

  protected Stream<Class> getExpressionFunctionClassStream() {
    return concat(
                  collectElements(ExpressionFunctions.class, ExpressionFunctions::value),
                  collectElements(com.zernova.sdk.api.annotation.ExpressionFunctions.class,
                                  com.zernova.sdk.api.annotation.ExpressionFunctions::value));
  }
}
