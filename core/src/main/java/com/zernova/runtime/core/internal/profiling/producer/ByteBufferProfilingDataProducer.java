/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.producer;

import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingProducerScope;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.ByteBufferProviderEventContext;
import com.zernova.runtime.api.profiling.type.TaskSchedulingProfilingEventType;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.profiling.ResettableProfilingDataProducer;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingDataProducerStatus;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;

import java.util.Map;
import java.util.function.Function;

/**
 * A {@link ProfilingDataProducer} for producing data related to memory allocation/deallocation through a byte buffer provider.
 *
 * @see TaskSchedulingProfilingEventType
 * @see Scheduler
 * @since 4.5
 */
public class ByteBufferProfilingDataProducer
    implements ResettableProfilingDataProducer<ByteBufferProviderEventContext, Map> {

  private final DefaultProfilingService defaultProfilingService;
  private final ProfilingEventType<ByteBufferProviderEventContext> profilingEventType;
  private final ProfilingDataProducerStatus profilingProducerStatus;

  public ByteBufferProfilingDataProducer(DefaultProfilingService defaultProfilingService,
                                         ProfilingEventType<ByteBufferProviderEventContext> profilingEventType,
                                         ProfilingProducerScope profilingProducerScope,
                                         ProfilingFeatureFlaggingService featureFlaggingService) {
    this.defaultProfilingService = defaultProfilingService;
    this.profilingEventType = profilingEventType;
    this.profilingProducerStatus =
        featureFlaggingService.getProfilingDataProducerStatus(profilingEventType, profilingProducerScope);
  }

  @Override
  public void triggerProfilingEvent(ByteBufferProviderEventContext profilerEventContext) {
    if (profilingProducerStatus.isEnabled()) {
      defaultProfilingService.notifyEvent(profilerEventContext, profilingEventType);
    }
  }

  @Override
  public void triggerProfilingEvent(Map sourceData,
                                    Function<Map, ByteBufferProviderEventContext> transformation) {
    if (profilingProducerStatus.isEnabled()) {
      defaultProfilingService.notifyEvent(transformation.apply(sourceData), profilingEventType);
    }
  }

  @Override
  public void reset() {
    profilingProducerStatus.reset();
  }
}
