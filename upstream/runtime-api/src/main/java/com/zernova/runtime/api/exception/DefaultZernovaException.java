/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.exception;

import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.i18n.I18nMessageFactory;

/**
 * <code>ZernovaException</code> Is the base exception type for the Mule application any other exceptions thrown by Mule code will be
 * based on this exception.
 *
 * @since 1.0
 */
public class DefaultZernovaException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 3990670599515417655L;

  public DefaultZernovaException(String message) {
    this(I18nMessageFactory.createStaticMessage(message));
  }

  /**
   * @param message the exception message
   */
  public DefaultZernovaException(I18nMessage message) {
    super(message);
  }

  public DefaultZernovaException(String message, Throwable cause) {
    this(I18nMessageFactory.createStaticMessage(message), cause);
  }

  /**
   * @param message the exception message
   * @param cause   the exception that cause this exception to be thrown
   */
  public DefaultZernovaException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public DefaultZernovaException(Throwable cause) {
    super(cause);
  }
}
