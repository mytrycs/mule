/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * Creates instances of {@link com.zernova.runtime.api.streaming.bytes.CursorStreamProvider}
 *
 * @param <T> the generic type of the streams being cursored
 * @since 4.0
 */
@NoImplement
public interface CursorProviderFactory<T> {

  /**
   * Optionally creates a new {@link CursorProvider} to buffer the given {@code value}.
   * <p>
   * Implementations might resolve that the given stream is/should not be buffered and thus it will return the same given stream.
   * In that case, the stream will be unaltered.
   *
   * @param eventContext        the context of the event on which buffering is talking place
   * @param value               the stream to be cursored
   * @param originatingLocation the {@link ComponentLocation} where the cursor was created
   * @return A {@link CursorProvider} or the same given {@code inputStream}
   *
   * @since 4.4.0, 4.3.1
   */
  Object of(EventContext eventContext, T value, ComponentLocation originatingLocation);

  /**
   * Optionally creates a new {@link CursorProvider} to buffer the given {@code value}.
   * <p>
   * Implementations might resolve that the given stream is/should not be buffered and thus it will return the same given stream.
   * In that case, the stream will be unaltered.
   *
   * @param eventContext the context of the event on which buffering is talking place
   * @param value        the stream to be cursored
   * @return A {@link CursorProvider} or the same given {@code inputStream}
   *
   * @deprecated since 4.4.0 use {@link #of(EventContext, Object, ComponentLocation)} instead.
   */
  @Deprecated
  Object of(EventContext eventContext, T value);

  /**
   * Optionally creates a new {@link CursorProvider} to buffer the given {@code value}.
   * <p>
   * Implementations might resolve that the given stream is/should not be buffered and thus it will return the same given stream.
   * In that case, the stream will be unaltered.
   *
   * @param event               the event on which buffering is talking place
   * @param value               the stream to be cursored
   * @param originatingLocation the {@link ComponentLocation} where the cursor was created
   * @return A {@link CursorProvider} or the same given {@code inputStream}
   *
   * @since 4.4.0
   *
   * @deprecated use {@link #of(EventContext, Object, ComponentLocation)} instead.
   */
  @Deprecated
  Object of(CoreEvent event, T value, ComponentLocation originatingLocation);

  /**
   * Optionally creates a new {@link CursorProvider} to buffer the given {@code value}.
   * <p>
   * Implementations might resolve that the given stream is/should not be buffered and thus it will return the same given stream.
   * In that case, the stream will be unaltered.
   *
   * @param event the event on which buffering is talking place
   * @param value the stream to be cursored
   * @return A {@link CursorProvider} or the same given {@code inputStream}
   *
   * @deprecated use {@link #of(EventContext, Object, ComponentLocation)} instead.
   */
  @Deprecated
  Object of(CoreEvent event, T value);

  /**
   * @param value a stream
   * @return whether this factory can create a {@link Cursor} out of the given {@code value}
   */
  boolean accepts(Object value);
}
