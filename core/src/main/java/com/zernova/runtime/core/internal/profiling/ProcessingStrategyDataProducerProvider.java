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
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.ComponentProcessingStrategyProfilingEventContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.profiling.context.DefaultComponentProcessingStrategyProfilingEventContext;
import com.zernova.runtime.core.internal.profiling.producer.ComponentProcessingStrategyProfilingDataProducer;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;


/**
 * A {@link ProfilingDataProducerProvider} that provides {@link ComponentProcessingStrategyProfilingDataProducer}
 *
 * @since 4.5.0
 */
public class ProcessingStrategyDataProducerProvider
    implements ProfilingDataProducerProvider<DefaultComponentProcessingStrategyProfilingEventContext, CoreEvent> {

  private final DefaultProfilingService profilingService;
  private final ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType;
  private final ProfilingFeatureFlaggingService featureFlaggingService;

  public ProcessingStrategyDataProducerProvider(DefaultProfilingService profilingService,
                                                ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType,
                                                ProfilingFeatureFlaggingService featureFlaggingService) {
    this.profilingService = profilingService;
    this.profilingEventType = profilingEventType;
    this.featureFlaggingService = featureFlaggingService;

  }

  @Override
  public <T extends ProfilingEventContext, S> ResettableProfilingDataProducer<T, S> getProfilingDataProducer(
                                                                                                             ProfilingProducerScope profilingProducerScope) {
    return (ResettableProfilingDataProducer<T, S>) new ComponentProcessingStrategyProfilingDataProducer(profilingService,
                                                                                                        profilingEventType,
                                                                                                        profilingProducerScope,
                                                                                                        featureFlaggingService);
  }
}
