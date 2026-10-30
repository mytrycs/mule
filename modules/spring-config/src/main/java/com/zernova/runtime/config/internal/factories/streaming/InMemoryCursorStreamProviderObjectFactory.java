/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories.streaming;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENABLE_REPEATABLE_STREAMING_BYTES_EAGER_READ;

import com.zernova.runtime.api.util.DataSize;
import com.zernova.runtime.api.util.DataUnit;
import com.zernova.runtime.config.api.factories.streaming.AbstractCursorProviderObjectFactory;
import com.zernova.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;

public class InMemoryCursorStreamProviderObjectFactory
    extends AbstractCursorProviderObjectFactory<CursorStreamProviderFactory> {

  private final int initialBufferSize;
  private final int bufferSizeIncrement;
  private final int maxInMemorySize;
  private final DataUnit dataUnit;
  private final Boolean eagerRead;

  public InMemoryCursorStreamProviderObjectFactory(int initialBufferSize, int bufferSizeIncrement, int maxInMemorySize,
                                                   DataUnit dataUnit) {
    this(initialBufferSize, bufferSizeIncrement, maxInMemorySize, dataUnit, null);
  }

  public InMemoryCursorStreamProviderObjectFactory(int initialBufferSize, int bufferSizeIncrement, int maxInMemorySize,
                                                   DataUnit dataUnit, Boolean eagerRead) {
    this.initialBufferSize = initialBufferSize;
    this.bufferSizeIncrement = bufferSizeIncrement;
    this.maxInMemorySize = maxInMemorySize;
    this.dataUnit = dataUnit;
    this.eagerRead = eagerRead;
  }

  @Override
  public CursorStreamProviderFactory doGetObject() throws Exception {
    InMemoryCursorStreamConfig config = new InMemoryCursorStreamConfig(new DataSize(initialBufferSize, dataUnit),
                                                                       new DataSize(bufferSizeIncrement, dataUnit),
                                                                       new DataSize(maxInMemorySize, dataUnit),
                                                                       eagerRead == null
                                                                           ? ffService
                                                                               .isEnabled(ENABLE_REPEATABLE_STREAMING_BYTES_EAGER_READ)
                                                                           : eagerRead);

    return streamingManager.forBytes().getInMemoryCursorProviderFactory(config);
  }
}
