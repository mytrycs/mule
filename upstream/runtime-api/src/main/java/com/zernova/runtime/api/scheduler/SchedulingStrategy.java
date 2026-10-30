/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.scheduler;

import com.zernova.api.annotation.NoImplement;

import java.util.concurrent.ScheduledFuture;

/**
 * Strategy used by the {@link com.zernova.runtime.api.scheduler.Scheduler} for executing jobs.
 *
 * @since 4.4
 */
@NoImplement
public interface SchedulingStrategy {

  /**
   * Schedules a job.
   *
   * @param executor the corresponding {@link com.zernova.runtime.api.scheduler.Scheduler} instance.
   * @param job      The {@link Runnable} job that has to be executed.
   * @return the newly scheduled job.
   * @throws NullPointerException In case the scheduled job handler is null.
   */
  ScheduledFuture<?> schedule(com.zernova.runtime.api.scheduler.Scheduler executor, Runnable job);
}
