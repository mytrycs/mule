/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import com.zernova.runtime.api.profiling.ProfilingEventContext;
import com.zernova.runtime.api.profiling.ProfilingProducerScope;
import com.zernova.runtime.api.profiling.threading.ThreadSnapshotCollector;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.ComponentThreadingProfilingEventContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.profiling.context.DefaultComponentThreadingProfilingEventContext;
import com.zernova.runtime.core.internal.profiling.producer.ComponentThreadingProfilingDataProducer;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;

/**
 * A {@link ProfilingDataProducerProvider} that provides {@link ComponentThreadingProfilingDataProducer}
 *
 * @since 4.5.0
 */
public class ComponentThreadingDataProducerProvider
    implements ProfilingDataProducerProvider<DefaultComponentThreadingProfilingEventContext, CoreEvent> {

  private final DefaultProfilingService profilingService;
  private final ProfilingEventType<ComponentThreadingProfilingEventContext> profilingEventType;
  private final ThreadSnapshotCollector threadSnapshotCollector;
  private final ProfilingFeatureFlaggingService featureFlaggingService;

  public ComponentThreadingDataProducerProvider(DefaultProfilingService profilingService,
                                                ProfilingEventType<ComponentThreadingProfilingEventContext> profilingEventType,
                                                ThreadSnapshotCollector threadSnapshotCollector,
                                                ProfilingFeatureFlaggingService featureFlaggingService) {
    this.profilingService = profilingService;
    this.profilingEventType = profilingEventType;
    this.threadSnapshotCollector = threadSnapshotCollector;
    this.featureFlaggingService = featureFlaggingService;

  }

  @Override
  public <T extends ProfilingEventContext, S> ResettableProfilingDataProducer<T, S> getProfilingDataProducer(
                                                                                                             ProfilingProducerScope profilingProducerScope) {
    return (ResettableProfilingDataProducer<T, S>) new ComponentThreadingProfilingDataProducer(profilingService,
                                                                                               profilingEventType,
                                                                                               threadSnapshotCollector,
                                                                                               profilingProducerScope,
                                                                                               featureFlaggingService);
  }
}
