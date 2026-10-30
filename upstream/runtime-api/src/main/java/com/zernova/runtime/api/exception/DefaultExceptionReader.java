/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.exception;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.api.legacy.exception.ExceptionReader;

import java.util.Map;

/**
 * This is the default exception reader used if there is no specific one registered for the current exception.
 */
public final class DefaultExceptionReader implements ExceptionReader {

  @Override
  public String getMessage(Throwable t) {
    return t.getMessage();
  }

  @Override
  public Throwable getCause(Throwable t) {
    return t.getCause();
  }

  @Override
  public Class<?> getExceptionType() {
    return Throwable.class;
  }

  /**
   * Returns a map of the non-standard information stored on the exception
   *
   * @return a map of the non-standard information stored on the exception
   */
  @Override
  public Map<String, Object> getInfo(Throwable t) {
    return (t instanceof ZernovaException ? ((ZernovaException) t).getInfo() : emptyMap());
  }
}
