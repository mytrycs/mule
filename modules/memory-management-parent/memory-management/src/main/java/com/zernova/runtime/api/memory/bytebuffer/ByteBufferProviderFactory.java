/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.memory.bytebuffer;

import static com.zernova.runtime.internal.memory.bytebuffer.ByteBufferProviderBuilder.buildByteBufferProviderFrom;

import com.zernova.runtime.api.memory.provider.ByteBufferProvider;
import com.zernova.runtime.api.memory.provider.type.ByteBufferType;
import com.zernova.runtime.api.profiling.ProfilingService;

/**
 * Provides a factory method for creating basic {@link ByteBufferProvider} instaces.
 *
 * @since 4.8
 */
public interface ByteBufferProviderFactory {

  static ByteBufferProvider createByteBufferProvider(String name, ByteBufferType type, ProfilingService profilingService) {
    return buildByteBufferProviderFrom(type)
        .withName(name)
        .withProfilingService(profilingService)
        .build();
  }
}
