/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.streaming.exception;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;

/**
 * Signals that a buffer used for streaming has reached its maximum possible size and thus the streaming operation cannot
 * continue.
 *
 * @since 1.0
 */
public final class StreamingBufferSizeExceededException extends ZernovaRuntimeException {

  /**
   * Creates a new instance
   *
   * @param maxBufferSize the maximum size in bytes of the exceeded buffer
   */
  public StreamingBufferSizeExceededException(int maxBufferSize) {
    super(createStaticMessage("Buffer has exceeded its maximum size of " + maxBufferSize));
  }
}
