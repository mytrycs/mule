/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.config.profiling;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.profiling.ProfilingProducerScope;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;

/**
 * A {@link FeatureFlaggingService} with some extra functionalities that is only used internally by the runtime for profiling. For
 * using this extra functionalities, the implementation has to be injected using this interface.
 */
public interface ProfilingFeatureFlaggingService extends FeatureFlaggingService {

  /**
   * Gets a {@link ProfilingDataProducerStatus} according to a {@link ProfilingProducerScope}.
   *
   * @param profilingEventType    the {@link ProfilingEventType}.
   * @param profilngProducerScope the {@link ProfilingProducerScope}.
   * @return the {@link ProfilingDataProducerStatus}
   */
  ProfilingDataProducerStatus getProfilingDataProducerStatus(ProfilingEventType<?> profilingEventType,
                                                             ProfilingProducerScope profilngProducerScope);


  /**
   * Register a profiling feature associated to a {@link ProfilingEventType} using a profiling feature identifier.
   *
   * @param profilingEventType
   * @param identifier
   */
  void registerProfilingFeature(ProfilingEventType<?> profilingEventType,
                                String identifier);

  /**
   * enables/disables a feature
   *
   * @param profilingEventType the {@link ProfilingEventType} to toggle.
   * @param identifier         the identifier for the profiling feature.
   * @param status             the status to set.
   */
  void toggleProfilingFeature(ProfilingEventType<?> profilingEventType, String identifier, boolean status);
}
