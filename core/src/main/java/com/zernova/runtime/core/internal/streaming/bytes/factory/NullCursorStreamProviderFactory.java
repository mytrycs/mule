/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.bytes.factory;

import static com.zernova.runtime.core.internal.event.EventUtils.getRoot;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.bytes.ByteBufferManager;
import com.zernova.runtime.core.api.streaming.bytes.factory.AbstractCursorStreamProviderFactory;

import java.io.Closeable;
import java.io.InputStream;

/**
 * Implementation of {@link AbstractCursorStreamProviderFactory} which always returns the original stream without creating any
 * provider
 *
 * @since 4.0
 */
public class NullCursorStreamProviderFactory extends AbstractCursorStreamProviderFactory {

  /**
   * Implementation of {@link AbstractCursorStreamProviderFactory} which always returns the original stream without creating any
   * provider
   */
  public NullCursorStreamProviderFactory(ByteBufferManager bufferManager, StreamingManager streamingManager) {
    super(bufferManager, streamingManager);
  }

  /**
   * {@inheritDoc}
   *
   * @return the given {@code inputStream} wrapped in an {@link Either} instance
   */
  @Override
  protected Object resolve(InputStream inputStream, EventContext creatorRootEventContext, ComponentLocation originatingLocation) {
    streamingManager.manage((Closeable) inputStream, creatorRootEventContext);
    return inputStream;
  }

  @Override
  protected Object resolve(InputStream inputStream, CoreEvent event, ComponentLocation originatingLocation) {
    return resolve(inputStream, getRoot(event.getContext()), originatingLocation);
  }
}
