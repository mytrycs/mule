/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.object;

import com.zernova.runtime.api.streaming.object.CursorIterator;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.internal.streaming.IdentifiableCursorProvider;
import com.zernova.runtime.core.internal.streaming.ManagedCursorProvider;
import com.zernova.runtime.core.internal.streaming.MutableStreamingStatistics;

/**
 * {@link ManagedCursorProvider} implementation for {@link CursorIteratorProvider} instances
 *
 * @since 4.0
 */
public class ManagedCursorIteratorProvider extends ManagedCursorProvider<CursorIterator> implements CursorIteratorProvider {

  /**
   * {@inheritDoc}
   */
  public ManagedCursorIteratorProvider(IdentifiableCursorProvider<CursorIterator> delegate,
                                       MutableStreamingStatistics statistics) {
    super(delegate, statistics);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CursorIterator managedCursor(CursorIterator cursor) {
    return new ManagedCursorIterator(this, cursor, getJanitor());
  }

  @Override
  public boolean isManaged() {
    return true;
  }

}
