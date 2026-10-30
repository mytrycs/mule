/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.privileged;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;

import jakarta.inject.Inject;

public class PrivilegedNonBlockingComponentExecutor
    implements CompletableComponentExecutor<OperationModel>, Startable, Stoppable {

  public static final String OUTPUT = "Super Special Non Blocking";

  @Inject
  private SchedulerService schedulerService;
  private Scheduler ioScheduler;

  @Override
  public void start() throws ZernovaException {
    ioScheduler = schedulerService.ioScheduler();
  }

  @Override
  public void stop() throws ZernovaException {
    ioScheduler.stop();
  }

  @Override
  public void execute(ExecutionContext<OperationModel> executionContext, ExecutorCallback callback) {
    ioScheduler.schedule(() -> callback.complete(OUTPUT), 2, SECONDS);
  }
}
