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
import com.zernova.runtime.api.profiling.type.context.ExtensionProfilingEventContext;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.profiling.ResettableProfilingDataProducer;
import com.zernova.runtime.core.internal.profiling.consumer.annotations.RuntimeInternalProfilingDataConsumer;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingDataProducerStatus;

import java.util.function.Function;

/**
 * A {@link ProfilingDataProducer} for producing data related to extensions.
 *
 * @see com.zernova.runtime.api.profiling.type.ExtensionProfilingEventType
 * @since 4.4
 */
@RuntimeInternalProfilingDataConsumer
public class ExtensionProfilingDataProducer
    implements ResettableProfilingDataProducer<ExtensionProfilingEventContext, Object> {

  private final DefaultProfilingService defaultProfilingService;
  private final ProfilingEventType<ExtensionProfilingEventContext> profilingEventType;
  private ProfilingDataProducerStatus profilingProducerStatus;


  public ExtensionProfilingDataProducer(DefaultProfilingService defaultProfilingService,
                                        ProfilingEventType<ExtensionProfilingEventContext> profilingEventType,
                                        ProfilingProducerScope profilingProducerContext,
                                        ProfilingFeatureFlaggingService featureFlaggingService) {
    this.defaultProfilingService = defaultProfilingService;
    this.profilingEventType = profilingEventType;
    this.profilingProducerStatus =
        featureFlaggingService.getProfilingDataProducerStatus(profilingEventType, profilingProducerContext);
  }

  @Override
  public void triggerProfilingEvent(ExtensionProfilingEventContext profilingEventContext) {
    if (profilingProducerStatus == null) {
      reset();
    }

    if (profilingProducerStatus.isEnabled()) {
      defaultProfilingService.notifyEvent(profilingEventContext, profilingEventType);
    }
  }

  @Override
  public void triggerProfilingEvent(Object sourceData, Function<Object, ExtensionProfilingEventContext> transformation) {
    if (profilingProducerStatus.isEnabled()) {
      defaultProfilingService.notifyEvent(transformation.apply(sourceData), profilingEventType);
    }
  }

  @Override
  public void reset() {
    profilingProducerStatus.reset();
  }

}
