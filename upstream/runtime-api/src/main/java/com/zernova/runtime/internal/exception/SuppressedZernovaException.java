/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.exception;

import com.zernova.runtime.api.exception.ZernovaException;

/**
 * @see com.zernova.runtime.privileged.exception.SuppressedZernovaException
 * @since 1.2.3, 1.3
 */
public class SuppressedZernovaException extends com.zernova.runtime.privileged.exception.SuppressedZernovaException {

  private static final long serialVersionUID = -2020531237382360468L;

  /**
   * Constructs a new {@link SuppressedZernovaException}
   *
   * @param throwable       Exception that will be wrapped.
   * @param causeToSuppress The cause that wants to be suppressed. Cannot be null.
   */
  protected SuppressedZernovaException(Throwable throwable, ZernovaException causeToSuppress) {
    super(throwable, causeToSuppress);
  }
}
