/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.api.instrument;

import com.zernova.runtime.metrics.api.meter.Meter;

/**
 * A component that is used for reporting measurements.
 *
 * @since 4.5.0
 */
public interface Instrument {

  /**
   * @return the name of the instrument.
   */
  String getName();

  /**
   * @return the description of the instrument.
   */
  String getDescription();


  /**
   * @return the {@link Meter} associated to the {@link Instrument}
   */
  Meter getMeter();

  /**
   * Resets the {@link Instrument}
   */
  default void reset() {
    // Nothing to do by default.
  }
}
