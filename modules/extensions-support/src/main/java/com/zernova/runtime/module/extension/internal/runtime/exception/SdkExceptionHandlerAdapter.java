/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.exception;

import com.zernova.sdk.api.runtime.exception.ExceptionHandler;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler} into an sdk-api
 * {@link ExceptionHandler}
 *
 * @since 4.5.0
 */
public class SdkExceptionHandlerAdapter extends ExceptionHandler {

  private final com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler delegate;

  public static ExceptionHandler from(Object value) {
    if (value instanceof ExceptionHandler) {
      return (ExceptionHandler) value;
    } else if (value instanceof com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler) {
      return new SdkExceptionHandlerAdapter((com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler) value);
    } else {
      throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }
  }

  public SdkExceptionHandlerAdapter(com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler exceptionHandler) {
    this.delegate = exceptionHandler;
  }

  @Override
  public Exception enrichException(Exception e) {
    return delegate.enrichException(e);
  }
}
