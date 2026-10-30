/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.source;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.extension.api.ExtensionConstants.SCHEDULING_STRATEGY_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.runtime.source.BackPressureMode.WAIT;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.api.scheduler.SchedulingStrategy;
import com.zernova.runtime.core.api.source.scheduler.CronScheduler;
import com.zernova.runtime.core.api.source.scheduler.FixedFrequencyScheduler;
import com.zernova.runtime.extension.api.client.source.SourceParameterizer;
import com.zernova.runtime.extension.api.runtime.source.BackPressureMode;
import com.zernova.runtime.module.extension.internal.runtime.client.params.BaseComponentParameterizer;

import java.util.concurrent.TimeUnit;

/**
 * Default implementation for {@link SourceParameterizer}
 *
 * @since 4.5.0
 */
class DefaultSourceParameterizer extends BaseComponentParameterizer<SourceParameterizer> implements SourceParameterizer {

  private BackPressureMode backPressureMode = WAIT;
  private SchedulingStrategy schedulingStrategy = null;

  @Override
  public SourceParameterizer withBackPressureMode(BackPressureMode backPressureMode) {
    this.backPressureMode = backPressureMode;
    return this;
  }

  @Override
  public SourceParameterizer withFixedSchedulingStrategy(long frequency, TimeUnit timeUnit, long startDelay) {
    checkArgument(timeUnit != null, "timeUnit cannot be null");
    schedulingStrategy = new FixedFrequencyScheduler(frequency, startDelay, timeUnit);
    return this;
  }

  @Override
  public SourceParameterizer withCronSchedulingStrategy(String expression, String timeZone) {
    schedulingStrategy = new CronScheduler(expression, timeZone);
    return this;
  }

  @Override
  public <T extends ComponentModel> void setValuesOn(ComponentParameterization.Builder<T> builder) {
    super.setValuesOn(builder);
    if (schedulingStrategy != null) {
      builder.withParameter(SCHEDULING_STRATEGY_PARAMETER_NAME, schedulingStrategy);
    }
  }

  public BackPressureMode getBackPressureMode() {
    return backPressureMode;
  }
}
