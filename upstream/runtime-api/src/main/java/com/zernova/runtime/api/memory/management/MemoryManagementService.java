/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.memory.management;

import com.zernova.api.annotation.Experimental;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.memory.provider.ByteBufferPoolConfiguration;
import com.zernova.runtime.api.memory.provider.ByteBufferProvider;
import com.zernova.runtime.api.memory.provider.ByteBufferProviderConfiguration;
import com.zernova.runtime.api.memory.provider.type.ByteBufferType;

/**
 * Provides resources for memory management to the runtime.
 *
 * @since 4.5.0
 */
@Experimental
public interface MemoryManagementService extends Initialisable, Disposable {

  /**
   * Gets a named buffer provider according to a configuration.
   *
   * @param name              name for the the registration of the {@link ByteBufferProvider}
   * @param type              the {@link ByteBufferType}
   * @param poolConfiguration the {@link ByteBufferPoolConfiguration}
   * @return a {@link ByteBufferProvider}
   */
  ByteBufferProvider getByteBufferProvider(String name, ByteBufferType type,
                                           ByteBufferPoolConfiguration poolConfiguration);

  /**
   * Gets a default named buffer provider.
   *
   * @param name name for the the registration of the {@link ByteBufferProvider}
   * @param type the {@link ByteBufferType}
   * @return a {@link ByteBufferProvider}
   */
  ByteBufferProvider getByteBufferProvider(String name, ByteBufferType type);

  /**
   * Disposes a byte buffer provider.
   *
   * @param name name of the {@link ByteBufferProvider}
   */
  void disposeByteBufferProvider(String name);
}
