/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.message.error.matcher;

import com.zernova.runtime.api.message.ErrorType;

/**
 * Decides whether an error type matches a criteria defined by the implementation.
 *
 * @since 1.5
 */
public interface ErrorTypeMatcher {

  /**
   * @param errorType the {@link ErrorType} to check
   * @return {@code true} if a match is possible, {@code false} otherwise
   */
  boolean match(ErrorType errorType);

}

