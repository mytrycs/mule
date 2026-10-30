/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * <code>ZernovaContextException</code> is thrown when an exception occurs with Mule Context objects
 */
public final class ZernovaContextException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -1526680893293714178L;

  /**
   * @param message the exception message
   */
  public ZernovaContextException(I18nMessage message) {
    super(message);
  }

  /**
   * @param message the exception message
   * @param cause   the exception that cause this exception to be thrown
   */
  public ZernovaContextException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }
}
