/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming;

import com.zernova.runtime.core.api.streaming.StreamingStatistics;

/**
 * Augmented version of the {@link StreamingStatistics} contract which allows to manipulate the held values.
 *
 * @since 4.2.0
 */
public interface MutableStreamingStatistics extends StreamingStatistics {

  /**
   * Increases the number of open providers by one and returns the updated value
   *
   * @return the updated value
   */
  int incrementOpenProviders();

  /**
   * Decreases the number of open providers by one and returns the updated value
   *
   * @return the updated value
   */
  int decrementOpenProviders();

  /**
   * Increases the number of open cursors by one and returns the updated value
   *
   * @return the updated value
   */
  int incrementOpenCursors();

  /**
   * Decreases the number of open cursors by one and returns the updated value
   *
   * @return the updated value
   */
  int decrementOpenCursors();

  /**
   * Decreases the number of cursors by {@code howMany} and returns the updated value
   *
   * @param howMany how many cursors were closed
   * @return the updated value
   * @since 4.3.0
   */
  int decrementOpenCursors(int howMany);
}
