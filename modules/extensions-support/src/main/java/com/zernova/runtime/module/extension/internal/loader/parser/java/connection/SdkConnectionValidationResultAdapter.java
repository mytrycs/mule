/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.connection;

import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.message.ErrorType;

import java.util.Optional;

/**
 * Adapts a sdk-api {@link com.zernova.sdk.api.connectivity.ConnectionValidationResult} into a mule-api
 * {@link ConnectionValidationResult}.
 *
 * @since 4.5.0
 */
public class SdkConnectionValidationResultAdapter extends ConnectionValidationResult {

  private final com.zernova.sdk.api.connectivity.ConnectionValidationResult delegate;

  /**
   * Creates a new instance
   *
   * @param delegate the adapted instance
   */
  SdkConnectionValidationResultAdapter(com.zernova.sdk.api.connectivity.ConnectionValidationResult delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean isValid() {
    return delegate.isValid();
  }

  @Override
  public String getMessage() {
    return delegate.getMessage();
  }

  @Override
  public Optional<ErrorType> getErrorType() {
    return delegate.getErrorType();
  }

  @Override
  public Exception getException() {
    return delegate.getException();
  }
}
