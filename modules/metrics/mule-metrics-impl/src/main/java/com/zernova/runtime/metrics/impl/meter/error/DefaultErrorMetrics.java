/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.impl.meter.error;

import static com.zernova.runtime.api.exception.ExceptionHelper.getRootException;

import static java.util.Collections.singletonMap;

import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.metrics.api.error.ErrorIdProvider;
import com.zernova.runtime.metrics.api.error.ErrorMetrics;
import com.zernova.runtime.metrics.api.instrument.LongCounter;
import com.zernova.runtime.metrics.api.meter.Meter;

import com.zernova.runtime.metrics.impl.util.StackHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultErrorMetrics implements ErrorMetrics {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultErrorMetrics.class);

  private final LongCounter totalErrorsCounter;

  private final ErrorIdProvider errorIdProvider;

  public DefaultErrorMetrics(Meter errorMetricsMeter) {
    totalErrorsCounter = buildNewErrorCounter(errorMetricsMeter);
    this.errorIdProvider = getDefaultErrorIdProvider();
  }

  public DefaultErrorMetrics(Meter errorMetricsMeter, ErrorIdProvider errorIdProvider) {
    this.totalErrorsCounter = buildNewErrorCounter(errorMetricsMeter);
    this.errorIdProvider = errorIdProvider;
  }

  @Override
  public void measure(Error error) {
    try {
      measure(error.getCause());
    } catch (Throwable e) {
      LOGGER.error("Failed to measure an error. This will only affect the error metrics data.", e);
    }
  }

  @Override
  public void measure(Throwable error) {
    try {
      totalErrorsCounter.add(1, singletonMap("error-id", errorIdProvider.getErrorId(error)));
    } catch (Throwable e) {
      LOGGER.error("Failed to measure an error. This will only affect the error metrics data.", e);
    }
  }

  private LongCounter buildNewErrorCounter(Meter errorMetricsMeter) {
    return errorMetricsMeter.counterBuilder("error-count")
        .withDescription("Mule runtime error count").build();
  }

  private ErrorIdProvider getDefaultErrorIdProvider() {
    return new ErrorIdProvider() {

      private final StackHasher stackHasher = new StackHasher();

      @Override
      public String getErrorId(Error error) {
        return rootHash(error.getCause());
      }

      @Override
      public String getErrorId(Throwable error) {
        return rootHash(error);
      }

      private String rootHash(Throwable error) {
        return stackHasher.hexHash(getRootException(error));
      }
    };
  }
}
