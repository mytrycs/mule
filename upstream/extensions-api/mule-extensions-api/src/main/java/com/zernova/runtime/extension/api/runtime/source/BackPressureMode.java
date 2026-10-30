/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.source;

import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Describes the available back pressure strategies
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
public enum BackPressureMode {

  /**
   * Reject the event by throwing an error
   */
  FAIL,

  /**
   * Apply back pressure by blocking the {@link SourceCallback#handle(Result)} method
   */
  WAIT,

  /**
   * Apply back pressure by dropping the filtered event
   */
  DROP
}
