/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.globalconfig.api.exception;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.i18n.I18nMessage;

import java.io.IOException;

/**
 * Exception thrown when there's a problem in the global runtime configuration.
 *
 * @since 4.0
 */
public final class RuntimeGlobalConfigException extends ZernovaRuntimeException {

  public RuntimeGlobalConfigException(I18nMessage message) {
    super(message);
  }

  public RuntimeGlobalConfigException(Exception e) {
    super(e);
  }

  public RuntimeGlobalConfigException(I18nMessage message, IOException e) {
    super(message, e);
  }
}
