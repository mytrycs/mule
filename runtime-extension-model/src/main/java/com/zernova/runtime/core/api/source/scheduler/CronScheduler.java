/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.source.scheduler;

import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

import static java.lang.String.format;
import static java.util.TimeZone.getDefault;

import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.source.CronSchedulerConfiguration;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler for Cron scheduled jobs.
 *
 * @since 3.5.0
 */
@Alias("cron")
public final class CronScheduler extends PeriodicScheduler implements CronSchedulerConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CronScheduler.class);

  private static final String TZ_GMT_ID = "GMT";

  @Parameter
  @Expression(NOT_SUPPORTED)
  private String expression;

  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  private String timeZone;

  public CronScheduler() {}

  /**
   * Creates a new instance
   *
   * @since 4.5.0
   */
  public CronScheduler(String expression, String timeZone) {
    this.expression = expression;
    this.timeZone = timeZone;
  }

  @Override
  protected ScheduledFuture<?> doSchedule(Scheduler executor, Runnable job) {
    return executor.scheduleWithCronExpression(job, expression, resolveTimeZone(timeZone));
  }

  protected TimeZone resolveTimeZone(String name) {
    TimeZone resolvedTimeZone = timeZone == null ? getDefault() : java.util.TimeZone.getTimeZone(timeZone);
    if (!TZ_GMT_ID.equals(timeZone) && resolvedTimeZone.equals(java.util.TimeZone.getTimeZone(TZ_GMT_ID))) {
      logger.warn(format("Configured timezone '%s' is invalid in scheduler '%s'. Defaulting to %s", timeZone, name, TZ_GMT_ID));
    }
    return resolvedTimeZone;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public String getExpression() {
    return expression;
  }

  @Override
  public String getTimeZone() {
    return timeZone;
  }
}
