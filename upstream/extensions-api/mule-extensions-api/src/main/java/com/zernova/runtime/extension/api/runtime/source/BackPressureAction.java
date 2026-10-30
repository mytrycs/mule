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
 * Indicates which type of back pressure has the runtime applied on a give message.
 *
 * Notice that if compared to {@link BackPressureMode}, there's no equivalent to {@link BackPressureMode#WAIT}, that's because the
 * {@link BackPressureMode#WAIT} mode is simply about blocking executions of {@link SourceCallback#handle(Result)}
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
public enum BackPressureAction {

  /**
   * Indicates that en error has been thrown as the result of the back pressure
   */
  FAIL,

  /**
   * Indicates that the message will simply be dropped.
   */
  DROP
}
