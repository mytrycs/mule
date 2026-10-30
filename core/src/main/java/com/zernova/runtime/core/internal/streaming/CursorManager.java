/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming;

import static java.lang.Boolean.getBoolean;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.STREAMING_VERBOSE_PROPERTY;
import static com.zernova.runtime.core.internal.streaming.CursorUtils.unwrap;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.streaming.bytes.CursorStream;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.api.streaming.object.CursorIterator;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.internal.event.DefaultEventContext;
import com.zernova.runtime.core.internal.streaming.bytes.ManagedCursorStreamProvider;
import com.zernova.runtime.core.internal.streaming.object.ManagedCursorIteratorProvider;

/**
 * Keeps track of active {@link Cursor cursors} and their {@link CursorProvider providers}
 *
 * @since 4.0
 */
public class CursorManager {

  public static final boolean STREAMING_VERBOSE = getBoolean(STREAMING_VERBOSE_PROPERTY);

  private final MutableStreamingStatistics statistics;
  private final StreamingGhostBuster ghostBuster;

  /**
   * Creates a new instance
   *
   * @param statistics statistics which values should be kept updated
   */
  public CursorManager(MutableStreamingStatistics statistics, StreamingGhostBuster ghostBuster) {
    this.statistics = statistics instanceof NullStreamingStatistics ? null : statistics;
    this.ghostBuster = ghostBuster;
  }

  /**
   * Becomes aware of the given {@code provider} and returns a replacement provider which is managed by the runtime, allowing for
   * automatic resource handling
   *
   * @param provider     the provider to be tracked
   * @param ownerContext the root context of the event that created the provider
   * @return a {@link CursorProvider}
   */
  public CursorProvider manage(final CursorProvider provider, DefaultEventContext ownerContext) {
    CursorProvider innerDelegate = unwrap(provider);
    IdentifiableCursorProvider identifiable = IdentifiableCursorProviderDecorator.of(provider);

    ManagedCursorProvider managedProvider;
    if (innerDelegate instanceof CursorStreamProvider) {
      managedProvider = new ManagedCursorStreamProvider((IdentifiableCursorProvider<CursorStream>) identifiable, statistics);
    } else if (innerDelegate instanceof CursorIteratorProvider) {
      managedProvider = new ManagedCursorIteratorProvider((IdentifiableCursorProvider<CursorIterator>) identifiable, statistics);
    } else {
      throw new ZernovaRuntimeException(createStaticMessage("Unknown cursor provider type: " + innerDelegate.getClass().getName()));
    }

    return ownerContext.track(managedProvider, ghostBuster);
  }
}
