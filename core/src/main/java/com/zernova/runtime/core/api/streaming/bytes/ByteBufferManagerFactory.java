/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming.bytes;

/**
 * Factory for instances of {@link ByteBufferManager}.
 * <p>
 * Implementations are required to have a default constructor
 *
 * @since 4.3.0
 */
public interface ByteBufferManagerFactory {

  /**
   * @return a new {@link ByteBufferManager}
   */
  ByteBufferManager create();
}
