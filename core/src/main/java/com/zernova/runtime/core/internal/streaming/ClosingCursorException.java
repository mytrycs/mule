/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming;

import com.zernova.runtime.api.streaming.Cursor;

import java.io.IOException;

/**
 * This {@link RuntimeException} is used for debugging purposes to track where a {@link Cursor} is closed.
 *
 * @since 4.4.0, 4.3.1
 */
public class ClosingCursorException extends IOException {

  private static final long serialVersionUID = 7821893999235016552L;

  public ClosingCursorException(String message) {
    super(message);
  }
}
