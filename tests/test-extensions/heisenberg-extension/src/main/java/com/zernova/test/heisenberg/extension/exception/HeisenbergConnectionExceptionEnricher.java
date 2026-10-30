/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.exception;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ZernovaFatalException;
import com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler;

public class HeisenbergConnectionExceptionEnricher extends ExceptionHandler {

  public static final String ENRICHED_MESSAGE = "Enriched Connection Exception: ";

  @Override
  public Exception enrichException(Exception e) {
    if (e instanceof ZernovaFatalException) {
      return e;
    }
    return new ConnectionException(ENRICHED_MESSAGE + e.getMessage(), e);
  }
}
