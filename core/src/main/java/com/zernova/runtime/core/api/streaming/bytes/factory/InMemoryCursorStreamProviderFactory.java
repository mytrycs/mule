/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming.bytes.factory;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.bytes.ByteBufferManager;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;

import java.io.InputStream;

/**
 * An implementation of {@link AbstractCursorStreamProviderFactory} which always generates instances of
 * {@link InMemoryCursorStreamProvider}
 *
 * @see InMemoryCursorStreamProvider
 * @since 4.0
 */
@NoExtend
public class InMemoryCursorStreamProviderFactory extends AbstractCursorStreamProviderFactory {

  private final InMemoryCursorStreamConfig config;

  /**
   * Creates a new instance
   *
   * @param config        the config for the generated providers
   * @param bufferManager the {@link ByteBufferManager} that will be used to allocate all buffers
   */
  public InMemoryCursorStreamProviderFactory(ByteBufferManager bufferManager,
                                             InMemoryCursorStreamConfig config,
                                             StreamingManager streamingManager) {
    super(bufferManager, streamingManager);
    this.config = config;
  }

  @Override
  protected Object resolve(InputStream inputStream, EventContext eventContext, ComponentLocation originatingLocation) {
    return doResolve(inputStream, originatingLocation);
  }

  /**
   * {@inheritDoc}
   *
   * @return a new {@link InMemoryCursorStreamProvider} wrapped in an {@link Either}
   */
  @Override
  protected Object resolve(InputStream inputStream, CoreEvent event, ComponentLocation originatingLocation) {
    return doResolve(inputStream, originatingLocation);
  }

  private Object doResolve(InputStream inputStream, ComponentLocation originatingLocation) {
    return new InMemoryCursorStreamProvider(inputStream, config, getBufferManager(), originatingLocation,
                                            trackCursorProviderClose);
  }
}
