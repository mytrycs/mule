/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context.lazy;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.ast.api.ComponentAst;

/**
 * Exception thrown when a requested {@link ComponentAst} in the configuration does not exists.
 */
public class NoSuchComponentModelException extends ZernovaRuntimeException {

  /**
   * Creates a new instance.
   *
   * @param message the exception message.
   */
  public NoSuchComponentModelException(I18nMessage message) {
    super(message);
  }

}
