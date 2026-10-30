/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.api;

import com.zernova.runtime.metrics.api.meter.Meter;
import com.zernova.runtime.metrics.api.meter.builder.MeterBuilder;

/**
 * A runtime managed provider to obtain {@link Meter}.
 *
 * @see Meter
 *
 * @since 4.5.0
 */
public interface MeterProvider {

  /**
   * No operation {@link MeterProvider} implementation. It will always return a no operation {@link MeterBuilder} implementation.
   */
  MeterProvider NO_OP = new NoOpMeterProvider();

  class NoOpMeterProvider implements MeterProvider {

    @Override
    public MeterBuilder getMeterBuilder(String meterName) {
      return MeterBuilder.NO_OP;
    }
  }

  /**
   * @param meterName the meter name.
   *
   * @return a {@link Meter} with the corresponding name.
   */
  MeterBuilder getMeterBuilder(String meterName);
}
