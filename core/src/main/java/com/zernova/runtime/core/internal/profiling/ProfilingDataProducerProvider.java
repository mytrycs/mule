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

/**
 * Provides {@link com.zernova.runtime.api.profiling.ProfilingDataProducer}
 *
 * @param <T>
 */
public interface ProfilingDataProducerProvider<T extends ProfilingEventContext, O> {

  /**
   * @param profilingProducerScope the {@link ProfilingProducerScope} that determines the profiling data producer to provide
   * @param <T>                    the {@link ProfilingEventContext} for the provided @{link
   *                               {@link com.zernova.runtime.api.profiling.ProfilingDataProducer}
   * @return the profiling data producer.
   */
  <T extends ProfilingEventContext, S> ResettableProfilingDataProducer<T, S> getProfilingDataProducer(
                                                                                                      ProfilingProducerScope profilingProducerScope);

}
