/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.objectbuilder;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;

/**
 * Utilities to power {@link ObjectBuilder} implementations
 *
 * @since 4.0
 */
class ObjectBuilderUtils {

  /**
   * Creates a new instance of the given {@code prototypeClass}
   *
   * @param prototypeClass the class of the object to create
   * @param <T>            the generic type of the {@code prototypeClass}
   * @return a new instance
   */
  public static <T> T createInstance(Class<T> prototypeClass) {
    try {
      return prototypeClass.newInstance();
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of " + prototypeClass), e);
    }
  }

  private ObjectBuilderUtils() {}
}
