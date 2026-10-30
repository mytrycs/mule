/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.sse;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;

import java.util.Optional;

/**
 * Server-sent event (SSE).
 * <p>
 * This API is EXPERIMENTAL. Do not use it until it is stable.
 *
 * @since 4.9.3, 4.10.0
 */
@Experimental
@NoImplement
public interface ServerSentEvent {

  /**
   * @return the event name, the topic of the event.
   */
  String getName();

  /**
   * @return the full data as string.
   */
  String getData();

  /**
   * @return event id.
   */
  Optional<String> getId();

  /**
   * The server may set the reconnection delay.
   *
   * @return the new retry delay, if the server configured it.
   */
  Optional<Long> getRetryDelay();
}
