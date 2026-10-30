/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.object;

import com.zernova.runtime.core.internal.streaming.object.factory.InMemoryCursorIteratorProviderFactory;
import com.zernova.runtime.core.internal.streaming.object.factory.NullCursorIteratorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import com.zernova.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;
import com.zernova.runtime.core.api.streaming.object.ObjectStreamingManager;

/**
 * Default implementation of {@link ObjectStreamingManager}
 *
 * @since 4.0
 */
public class DefaultObjectStreamingManager implements ObjectStreamingManager {

  protected final StreamingManager streamingManager;

  public DefaultObjectStreamingManager(StreamingManager streamingManager) {
    this.streamingManager = streamingManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorIteratorProviderFactory getInMemoryCursorProviderFactory(InMemoryCursorIteratorConfig config) {
    return new InMemoryCursorIteratorProviderFactory(config, streamingManager);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorIteratorProviderFactory getNullCursorProviderFactory() {
    return new NullCursorIteratorProviderFactory(streamingManager);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorIteratorProviderFactory getDefaultCursorProviderFactory() {
    return getInMemoryCursorProviderFactory(InMemoryCursorIteratorConfig.getDefault());
  }
}
