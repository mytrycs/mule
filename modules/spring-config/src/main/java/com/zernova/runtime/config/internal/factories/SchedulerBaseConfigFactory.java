/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.zernova.runtime.api.scheduler.SchedulerConfig.config;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;

import jakarta.inject.Inject;

import org.springframework.beans.factory.FactoryBean;

/**
 * Builds a base {@link SchedulerConfig} to be provided to the calls to {@link SchedulerService}.
 *
 * @since 4.0
 */
public class SchedulerBaseConfigFactory implements FactoryBean<SchedulerConfig> {

  @Inject
  private ZernovaContext muleContext;

  @Override
  public Class<?> getObjectType() {
    return SchedulerConfig.class;
  }

  @Override
  public SchedulerConfig getObject() throws Exception {
    return config().withPrefix(muleContext.getConfiguration().getId())
        .withShutdownTimeout(() -> muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS);
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
