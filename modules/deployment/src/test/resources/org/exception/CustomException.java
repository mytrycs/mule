/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.exception;

import com.zernova.runtime.api.exception.ZernovaException;

public class CustomException extends ZernovaException {

  private static final String MESSAGE = "Error";

  @Override
  public String getDetailedMessage() {
    return MESSAGE;
  }

  @Override
  public String getVerboseMessage() {
    return MESSAGE;
  }

  @Override
  public String getSummaryMessage() {
    return MESSAGE;
  }
}