/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.bytes.ByteStreamingManager;
import com.zernova.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import com.zernova.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import com.zernova.runtime.core.api.streaming.object.ObjectStreamingManager;

import java.io.Closeable;
import java.io.InputStream;

/**
 * Manages resources dedicated to perform streaming of bytes or objects, so that the runtime can keep track of them, enforce
 * policies and make sure that all resources are reclaimed once no longer needed.
 *
 * @since 4.0
 */
@NoImplement
public interface StreamingManager {

  /**
   * @return a delegate manager to be used when streaming bytes
   */
  ByteStreamingManager forBytes();

  /**
   * @return a delegate manager to be used when streaming objects
   */
  ObjectStreamingManager forObjects();

  /**
   * @return statistics about the ongoing streaming operations
   */
  StreamingStatistics getStreamingStatistics();

  /**
   * Becomes aware of the given {@code provider} and returns a replacement provider which is managed by the runtime, allowing for
   * automatic resource handling
   *
   * @param provider                the provider to be tracked
   * @param creatorRootEventContext the event context on which the stream was created
   * @return a {@link CursorProvider}
   */
  CursorProvider manage(CursorProvider provider, EventContext creatorRootEventContext);

  /**
   * Becomes aware of the given {@code provider} and returns a replacement provider which is managed by the runtime, allowing for
   * automatic resource handling
   *
   * @param provider     the provider to be tracked
   * @param creatorEvent the event that created the provider
   * @return a {@link CursorProvider}
   * @deprecated Use {@link #manage(CursorProvider, EventContext)} instead.
   */
  @Deprecated
  CursorProvider manage(CursorProvider provider, CoreEvent creatorEvent);

  /**
   * Becomes aware of the given {@code inputStream} and makes sure it is closed by the time the given
   * {@code creatorRootEventContext} (and all its parent events) are completed.
   * <p>
   * If {@code inputStream} is a {@link Cursor} then nothing happens. Use {@link #manage(CursorProvider, CoreEvent)} for those
   * cases.
   *
   * @param inputStream             the stream to track
   * @param creatorRootEventContext the event context on which the stream was created
   * @deprecated since 4.2.2 - 4.3.0. Use {@link #manage(Closeable, EventContext)} instead
   */
  @Deprecated
  void manage(InputStream inputStream, EventContext creatorRootEventContext);

  /**
   * Becomes aware of the given {@code closeable} stream and makes sure it is closed by the time the given
   * {@code creatorRootEventContext} (and all its parent events) are completed.
   * <p>
   * If {@code closeable} is a {@link Cursor} then nothing happens. Use {@link #manage(CursorProvider, CoreEvent)} for those
   * cases.
   *
   * @param closeable               a closeable stream to be tracked
   * @param creatorRootEventContext the event context on which the stream was created
   */
  void manage(Closeable closeable, EventContext creatorRootEventContext);

  /**
   * Becomes aware of the given {@code inputStream} and makes sure it is closed by the time the given {@code creatorEvent} (and
   * all its parent events) are completed.
   * <p>
   * If {@code inputStream} is a {@link Cursor} then nothing happens. Use {@link #manage(CursorProvider, CoreEvent)} for those
   * cases.
   *
   * @param inputStream  the stream to track
   * @param creatorEvent the event on which the stream was created
   *
   * @deprecated Use {@link #manage(InputStream, EventContext)} instead
   */
  @Deprecated
  void manage(InputStream inputStream, CoreEvent creatorEvent);

  /**
   * Given a {@link CursorProviderFactory} that is either a {@link CursorStreamProviderFactory} or a
   * {@link CursorIteratorProviderFactory}, returns the pair of provider factories that must be used for streaming bytes and
   * objects. This way, when you are given a provider factory for streaming objects, but those objects are or have a stream of
   * bytes, you will also have the appropriate provider factory for those streams to be managed.
   *
   * @param provider the provider for which you want the correspondent byte and object providers.
   * @return a pair of {@link CursorProviderFactory}s to be used, one for streaming bytes, and the other for streaming objects
   *
   * @since 4.3
   */
  Pair<CursorStreamProviderFactory, CursorIteratorProviderFactory> getPairFor(CursorProviderFactory provider);

}
