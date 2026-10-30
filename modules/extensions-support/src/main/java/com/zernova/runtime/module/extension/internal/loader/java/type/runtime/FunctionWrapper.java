/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.runtime;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.module.extension.api.loader.java.type.FunctionContainerElement;
import com.zernova.runtime.module.extension.api.loader.java.type.FunctionElement;

import java.lang.reflect.Method;

/**
 * {@link FunctionElement} implementation which works with Classes
 *
 * @since 4.1
 */
public class FunctionWrapper extends MethodWrapper<FunctionContainerElement> implements FunctionElement {

  public FunctionWrapper(Method method, ClassTypeLoader typeLoader) {
    super(method, typeLoader);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionContainerElement getEnclosingType() {
    return new FunctionContainerWrapper(method.getDeclaringClass(), typeLoader);
  }
}
