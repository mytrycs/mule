/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.loader.java.type;

import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getMethodReturnAttributesType;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getMethodReturnType;
import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.NamedObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.ExecutableElement;

/**
 * A contract for an element to be considered as a Method
 *
 * @since 4.0
 */
@NoImplement
public interface MethodElement<T extends Type>
    extends WithParameters, WithReturnType, NamedObject, WithAnnotations, WithAlias, WithDeclaringClass, WithElement {

  /**
   * @return The represented method
   */
  Optional<Method> getMethod();

  /**
   * @return The list of exceptions types that a method can throw
   */
  List<Type> getExceptionTypes();

  /**
   * @return The {@link OperationContainerElement} which contains the current {@link MethodElement}
   */
  T getEnclosingType();

  @Override
  Optional<ExecutableElement> getElement();

  @Override
  default MetadataType getReturnMetadataType() {
    return getMethodReturnType(this);
  }

  @Override
  default MetadataType getAttributesMetadataType() {
    return getMethodReturnAttributesType(this);
  }
}
