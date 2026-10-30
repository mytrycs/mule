/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.exception;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.runtime.exception.SdkExceptionHandlerFactory;
import com.zernova.sdk.api.runtime.exception.ExceptionHandler;

public final class DefaultExceptionHandlerFactory implements SdkExceptionHandlerFactory {

  private final ExceptionHandler enricher;

  public DefaultExceptionHandlerFactory(Class<?> enricherType) {
    checkArgument(enricherType != null, "ExceptionEnricher type cannot be null");
    try {
      enricher = SdkExceptionHandlerAdapter.from(ClassUtils.instantiateClass(enricherType));
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create ExceptionEnricher of type " + enricherType.getName()),
                                     e);
    }
  }

  @Override
  public ExceptionHandler createHandler() {
    return enricher;
  }
}
