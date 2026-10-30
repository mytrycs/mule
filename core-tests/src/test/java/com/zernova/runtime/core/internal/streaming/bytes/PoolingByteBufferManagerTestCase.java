/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.bytes;

import static com.zernova.test.allure.AllureConstants.StreamingFeature.STREAMING;

import com.zernova.runtime.core.internal.streaming.MemoryManager;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;

@SmallTest
@Feature(STREAMING)
public class PoolingByteBufferManagerTestCase extends MemoryBoundByteBufferManagerContractTestCase {

  @Override
  protected MemoryBoundByteBufferManager createDefaultBoundBuffer() {
    return new PoolingByteBufferManager();
  }

  @Override
  protected MemoryBoundByteBufferManager createBuffer(MemoryManager memoryManager, int capacity) {
    return new PoolingByteBufferManager(memoryManager, 2, capacity);
  }
}
