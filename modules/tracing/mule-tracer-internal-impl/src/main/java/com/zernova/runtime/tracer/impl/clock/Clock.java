/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.clock;

import com.zernova.runtime.tracer.impl.span.InternalSpan;

import static com.zernova.runtime.tracer.impl.clock.SystemNanoTimeClock.getInstance;

/**
 * A clock used for tracing and measure the duration of {@link InternalSpan}.
 *
 * @since 4.5.0
 */
public interface Clock {

  /**
   * @return default implementation of clock.
   */
  static Clock getDefault() {
    return getInstance();
  }

  /**
   * @return the current epoch timestamp in nanos from this clock.
   */
  long now();
}
