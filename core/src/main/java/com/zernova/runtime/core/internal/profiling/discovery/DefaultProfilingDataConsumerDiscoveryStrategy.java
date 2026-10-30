/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.discovery;

import static com.google.common.collect.ImmutableSet.of;

import com.zernova.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;
import com.zernova.runtime.core.internal.profiling.consumer.LoggerByteBufferAllocationProfilingDataConsumer;
import com.zernova.runtime.core.internal.profiling.consumer.ComponentProcessingStrategyDataConsumer;
import com.zernova.runtime.core.internal.profiling.consumer.LoggerComponentThreadingDataConsumer;
import com.zernova.runtime.core.internal.profiling.consumer.TaskSchedulingLoggerDataConsumer;
import com.zernova.runtime.core.internal.profiling.consumer.TransactionLoggerDataConsumer;

import java.util.Set;

/**
 * Simple {@link ProfilingDataConsumerDiscoveryStrategy} that programmatically generates the data consumers.
 *
 * @since 4.4
 */
public class DefaultProfilingDataConsumerDiscoveryStrategy implements ProfilingDataConsumerDiscoveryStrategy {

  private final InternalProfilingService profilingService;

  public DefaultProfilingDataConsumerDiscoveryStrategy(InternalProfilingService profilingService) {
    this.profilingService = profilingService;
  }

  @Override
  public Set<ProfilingDataConsumer<?>> discover() {
    return of(new LoggerByteBufferAllocationProfilingDataConsumer(),
              new ComponentProcessingStrategyDataConsumer(profilingService),
              new LoggerComponentThreadingDataConsumer(),
              new TransactionLoggerDataConsumer(),
              new TaskSchedulingLoggerDataConsumer());
  }

}
