/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.source.scheduler;

import static java.util.Objects.requireNonNull;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.source.SchedulerConfiguration;

import java.util.concurrent.ScheduledFuture;

/**
 * Scheduler for periodic jobs.
 *
 * @since 3.5.0, moved from {@link com.zernova.runtime.core.api.schedule.SchedulerFactory}.
 */
@NoExtend
public abstract class PeriodicScheduler extends AbstractComponent implements Scheduler, SchedulerConfiguration {

  /**
   * {@inheritDoc}
   */
  @Override
  public final ScheduledFuture<?> schedule(com.zernova.runtime.api.scheduler.Scheduler executor, Runnable job) {
    return requireNonNull(doSchedule(executor, job));
  }

  /**
   * Template method to delegate the scheduling of the job.
   *
   * @param executor the corresponding {@link com.zernova.runtime.api.scheduler.Scheduler} instance.
   * @param job      The {@link Runnable} job that has to be executed.
   * @return the newly scheduled job.
   */
  protected abstract ScheduledFuture<?> doSchedule(com.zernova.runtime.api.scheduler.Scheduler executor, Runnable job);

}
