/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.consumer.tracing.operations;

import static com.zernova.runtime.core.internal.profiling.consumer.ComponentProfilingUtils.getProcessingStrategyComponentInfoMap;

import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.ComponentProcessingStrategyProfilingEventContext;

import com.google.gson.Gson;
import org.slf4j.Logger;

/**
 * A {@link ProfilingExecutionOperation} that logs the data of a profiling event.
 *
 * @since 4.5.0
 */
public class LoggerProfilingEventOperation implements
    ProfilingExecutionOperation<ComponentProcessingStrategyProfilingEventContext> {

  private final Logger logger;
  private final ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType;

  private final Gson gson = new Gson();

  public LoggerProfilingEventOperation(Logger logger,
                                       ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType) {
    this.logger = logger;
    this.profilingEventType = profilingEventType;
  }

  @Override
  public void execute(ComponentProcessingStrategyProfilingEventContext profilingEventContext) {
    if (logger.isDebugEnabled()) {
      logger.debug(gson.toJson(getProcessingStrategyComponentInfoMap(profilingEventType, profilingEventContext)));
    }
  }
}
