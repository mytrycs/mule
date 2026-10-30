/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories.streaming;

import com.zernova.runtime.config.api.factories.streaming.AbstractCursorProviderObjectFactory;
import com.zernova.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import com.zernova.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;

public class InMemoryCursorIteratorProviderObjectFactory
    extends AbstractCursorProviderObjectFactory<CursorIteratorProviderFactory> {

  private final int initialBufferSize;
  private final int bufferSizeIncrement;
  private final int maxInMemoryInstances;

  public InMemoryCursorIteratorProviderObjectFactory(int initialBufferSize, int bufferSizeIncrement, int maxInMemoryInstances) {
    this.initialBufferSize = initialBufferSize;
    this.bufferSizeIncrement = bufferSizeIncrement;
    this.maxInMemoryInstances = maxInMemoryInstances;
  }

  @Override
  public CursorIteratorProviderFactory doGetObject() throws Exception {
    InMemoryCursorIteratorConfig config =
        new InMemoryCursorIteratorConfig(initialBufferSize, bufferSizeIncrement, maxInMemoryInstances);

    return streamingManager.forObjects().getInMemoryCursorProviderFactory(config);
  }
}
