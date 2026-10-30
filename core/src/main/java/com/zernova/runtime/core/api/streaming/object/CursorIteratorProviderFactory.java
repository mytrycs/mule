/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming.object;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.internal.streaming.object.factory.NullCursorIteratorProviderFactory;

import java.util.Iterator;

/**
 * Specialization of {@link CursorProviderFactory} which creates {@link CursorIteratorProvider} instances out of {@link Iterator}
 * instances
 *
 * @since 4.0
 */
@NoImplement
public interface CursorIteratorProviderFactory extends CursorProviderFactory<Iterator> {

  /**
   * @param streamingManager the {@link StreamingManager} to handle the {@link Iterator}s.
   * @return a {@link CursorIteratorProviderFactory} which always returns the original iterator without creating any provider.
   */
  static CursorIteratorProviderFactory nullCursorIteratorProviderFactory(StreamingManager streamingManager) {
    return new NullCursorIteratorProviderFactory(streamingManager);
  }

}
