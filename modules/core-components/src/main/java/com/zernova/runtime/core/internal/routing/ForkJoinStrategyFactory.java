/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.privileged.routing.CompositeRoutingException;


public interface ForkJoinStrategyFactory {

  /**
   * Create instance of {@link ForkJoinStrategy}
   *
   * @param processingStrategy       processing strategy to use
   * @param maxConcurrency           maximum number of routes/parts to be processed in parallel.
   * @param delayErrors              if all routers/parts should be processed regardless of errors and a
   *                                 {@link CompositeRoutingException} thrown or not.
   * @param timeout                  the amount of time in milliseconds before timeout.
   * @param timeoutScheduler         a {@link Scheduler} for emitting the timeout events.
   * @param timeoutErrorType         the timeout error type.
   * @param timeoutBlockingScheduler a scheduler for performing potentially blocking cleanup tasks on timeout.
   * @param isDetailedLogEnabled     if detailed error/exception message will be provided or not.
   * @return new instance of {@link ForkJoinStrategy}
   */
  ForkJoinStrategy createForkJoinStrategy(ProcessingStrategy processingStrategy, int maxConcurrency, boolean delayErrors,
                                          long timeout, Scheduler timeoutScheduler, ErrorType timeoutErrorType,
                                          Scheduler timeoutBlockingScheduler, boolean isDetailedLogEnabled);

  DataType getResultDataType();

}
