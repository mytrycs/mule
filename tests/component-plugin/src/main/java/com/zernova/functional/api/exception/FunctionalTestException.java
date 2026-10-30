/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.exception;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaException;

public class FunctionalTestException extends ZernovaException {

  public static final String EXCEPTION_MESSAGE = "Functional Test Service Exception";

  public FunctionalTestException() {
    this(EXCEPTION_MESSAGE);
  }

  public FunctionalTestException(String exceptionText) {
    super(createStaticMessage(exceptionText));
  }
}
