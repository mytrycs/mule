/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.expression;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * Thrown by the {@link com.zernova.runtime.core.api.el.ExpressionManager} when a failure occurs evaluating an expression.
 */
@NoExtend
public class ExpressionRuntimeException extends ZernovaRuntimeException {

  private static final long serialVersionUID = -8632366166228091959L;

  /**
   * @param message the exception message
   */
  public ExpressionRuntimeException(I18nMessage message) {
    super(message);
  }

  /**
   * @param message the exception message
   * @param cause   the exception that triggered this exception
   */
  public ExpressionRuntimeException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }
}
