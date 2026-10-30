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
import com.zernova.runtime.api.profiling.type.TransactionProfilingEventType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.profiling.ResettableProfilingDataProducer;
import com.zernova.runtime.api.profiling.type.context.TransactionProfilingEventContext;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingDataProducerStatus;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;

import java.util.function.Function;

/**
 * A {@link ProfilingDataProducer} for producing data related to task scheduling.
 *
 * @see TransactionProfilingEventType
 * @since 4.5
 */
public class TransactionProfilingDataProducer implements
    ResettableProfilingDataProducer<TransactionProfilingEventContext, CoreEvent> {

  private final DefaultProfilingService defaultProfilingService;
  private final ProfilingEventType<TransactionProfilingEventContext> profilingEventType;
  private final ProfilingDataProducerStatus profilingProducerStatus;

  public TransactionProfilingDataProducer(DefaultProfilingService defaultProfilingService,
                                          ProfilingEventType<TransactionProfilingEventContext> profilingEventType,
                                          ProfilingProducerScope profilingProducerScope,
                                          ProfilingFeatureFlaggingService featureFlaggingService) {
    this.defaultProfilingService = defaultProfilingService;
    this.profilingEventType = profilingEventType;
    this.profilingProducerStatus =
        featureFlaggingService.getProfilingDataProducerStatus(profilingEventType, profilingProducerScope);
  }

  @Override
  public void reset() {
    profilingProducerStatus.reset();
  }

  @Override
  public void triggerProfilingEvent(TransactionProfilingEventContext transactionProfilingEventContext) {
    if (profilingProducerStatus.isEnabled()) {
      defaultProfilingService.notifyEvent(transactionProfilingEventContext, profilingEventType);
    }
  }

  @Override
  public void triggerProfilingEvent(CoreEvent coreEvent, Function<CoreEvent, TransactionProfilingEventContext> transformation) {
    if (profilingProducerStatus.isEnabled()) {
      defaultProfilingService.notifyEvent(transformation.apply(coreEvent), profilingEventType);
    }
  }
}
