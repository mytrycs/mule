/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer;

import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.core.api.transformer.TransformerException;

/**
 * An exception that is thrown by resolver classes responsible for finding objects in the registry based on particular criteria
 */
public class ResolverException extends TransformerException {

  private static final long serialVersionUID = 389248508135507438L;

  public ResolverException(I18nMessage message) {
    super(message);
  }

  public ResolverException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }
}
