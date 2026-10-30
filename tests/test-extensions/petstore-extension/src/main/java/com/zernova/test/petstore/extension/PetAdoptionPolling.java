/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.meta.ExpressionSupport;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.source.scheduler.FixedFrequencyScheduler;
import com.zernova.runtime.core.api.source.scheduler.Scheduler;
import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.Summary;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PetAdoptionPolling {

  private static final long DEFAULT_FREQUENCY = 1000L;
  private static final long DEFAULT_START_DELAY = 1000L;
  private static final TimeUnit DEFAULT_TIME_UNIT = MILLISECONDS;

  @Parameter
  @Summary("Configures the scheduler that triggers the polling")
  @ParameterDsl(allowReferences = false)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private Scheduler schedulingStrategy;

  public Scheduler getSchedulingStrategy() {
    return schedulingStrategy;
  }

  public void setSchedulingStrategy(Scheduler schedulingStrategy) {
    this.schedulingStrategy = schedulingStrategy;
  }

  public com.zernova.runtime.api.scheduler.Scheduler createExecutor(String subscriberName, SchedulerService schedulerService) {
    initaliseStrategy();

    return schedulerService.customScheduler(SchedulerConfig.config()
        .withName(subscriberName)
        .withMaxConcurrentTasks(1)
        .withWaitAllowed(false));
  }

  private void initaliseStrategy() {
    if (schedulingStrategy == null) {
      FixedFrequencyScheduler strategy = new FixedFrequencyScheduler();
      strategy.setTimeUnit(DEFAULT_TIME_UNIT);
      strategy.setFrequency(DEFAULT_FREQUENCY);
      strategy.setStartDelay(DEFAULT_START_DELAY);
      schedulingStrategy = strategy;
    }
  }

  public Optional<Long> getFrequency() {
    return schedulingStrategy instanceof FixedFrequencyScheduler
        ? Optional.of(((FixedFrequencyScheduler) schedulingStrategy).getFrequency())
        : Optional.empty();
  }
}
