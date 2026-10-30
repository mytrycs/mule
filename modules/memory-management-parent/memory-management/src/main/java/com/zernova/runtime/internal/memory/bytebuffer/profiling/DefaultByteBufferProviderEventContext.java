/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.memory.bytebuffer.profiling;

import com.zernova.runtime.api.profiling.type.context.ByteBufferProviderEventContext;

/**
 * A default implementation of {@link com.zernova.runtime.api.profiling.type.context.ByteBufferProviderEventContext}
 */
public class DefaultByteBufferProviderEventContext implements ByteBufferProviderEventContext {

  private final String byteBufferProviderName;
  private final long triggerTimestamp;
  private final int size;

  public DefaultByteBufferProviderEventContext(String byteBufferProviderName, long triggerTimestamp, int size) {
    this.byteBufferProviderName = byteBufferProviderName;
    this.triggerTimestamp = triggerTimestamp;
    this.size = size;
  }

  @Override
  public long getTriggerTimestamp() {
    return triggerTimestamp;
  }

  @Override
  public String getByteBufferProviderName() {
    return byteBufferProviderName;
  }

  @Override
  public int size() {
    return size;
  }
}
