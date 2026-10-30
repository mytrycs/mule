/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.exception;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;

/**
 * Signals that the SDK invoked an extension's method which threw an exception. The exception thrown will be available as the
 * cause.
 * <p>
 * Because this is a {@link ZernovaRuntimeException}, it will not automatically fill the stack trace, providing a performance boost.
 *
 * @since 4.3.0
 */
public class SdkMethodInvocationException extends ZernovaRuntimeException {

  private static final long serialVersionUID = 8708990792854682118L;

  public SdkMethodInvocationException(Throwable cause) {
    super(cause);
  }
}
