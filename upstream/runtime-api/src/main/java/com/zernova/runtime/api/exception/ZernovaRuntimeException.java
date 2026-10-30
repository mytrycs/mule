/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.exception;

import static com.zernova.runtime.api.exception.ZernovaException.isVerboseExceptions;

import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * {@code ZernovaRuntimeException} Is the base runtime exception type for the Mule Server any other runtimes exceptions thrown by
 * Mule code will use or be based on this exception. Runtime exceptions in mule are only ever thrown where the method is not
 * declared to throw an exception and the exception is serious.
 *
 * @since 1.0
 */
public class ZernovaRuntimeException extends RuntimeException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 6728041560892553159L;

  /**
   * @param message the exception message
   */
  public ZernovaRuntimeException(I18nMessage message) {
    this(message, null);
  }

  /**
   * @param message the exception message
   * @param cause   the exception that triggered this exception
   */
  public ZernovaRuntimeException(I18nMessage message, Throwable cause) {
    this(message != null ? message.getMessage() : null, cause);
  }

  /**
   * @param cause the exception that triggered this exception
   */
  public ZernovaRuntimeException(Throwable cause) {
    this(cause.toString(), cause);
  }

  private ZernovaRuntimeException(String message, Throwable cause) {
    super(message, cause, true, isVerboseExceptions());
  }

}
