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
import com.zernova.runtime.api.profiling.type.context.ByteBufferProviderEventContext;
import com.zernova.runtime.core.internal.profiling.producer.ByteBufferProfilingDataProducer;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;

import java.util.Map;

/**
 * A {@link ProfilingDataProducerProvider} that provides {@link ByteBufferProfilingDataProducer}
 *
 * @since 4.5.0
 */
public class ByteBufferProviderDataProducerProvider
    implements ProfilingDataProducerProvider<ByteBufferProviderEventContext, Map> {

  private final DefaultProfilingService profilingService;
  private final ProfilingFeatureFlaggingService featureFlaggingService;
  private final ProfilingEventType<ByteBufferProviderEventContext> profilingEventType;

  public ByteBufferProviderDataProducerProvider(DefaultProfilingService profilingService,
                                                ProfilingEventType<ByteBufferProviderEventContext> profiliingEventType,
                                                ProfilingFeatureFlaggingService featureFlaggingService) {
    this.profilingService = profilingService;
    this.profilingEventType = profiliingEventType;
    this.featureFlaggingService = featureFlaggingService;
  }

  @Override
  public <T extends ProfilingEventContext, S> ResettableProfilingDataProducer<T, S> getProfilingDataProducer(ProfilingProducerScope profilingProducerScope) {
    return (ResettableProfilingDataProducer<T, S>) new ByteBufferProfilingDataProducer(profilingService, profilingEventType,
                                                                                       profilingProducerScope,
                                                                                       featureFlaggingService);
  }
}
