/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.sdk.api.runtime.source.BackPressureMode.DROP;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.annotation.source.BackPressure;
import com.zernova.test.heisenberg.extension.model.types.DEAOfficerAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import jakarta.inject.Inject;

@Alias("dea-radio")
@BackPressure(defaultMode = DROP, supportedModes = DROP)
public class DEARadioSource extends Source<List<Result<String, DEAOfficerAttributes>>, Object> {

  public static final int MESSAGES_PER_POLL = 5;
  public static final String MESSAGE_TEXT = "I heard Heisenberg is in the neighborhood";

  @Inject
  private SchedulerService schedulerService;

  @Inject
  private SchedulerConfig baseConfig;

  private Scheduler executor;
  private ScheduledFuture sourceCallbakHandleTask;

  private Random random = new Random();

  @Override
  public void onStart(SourceCallback<List<Result<String, DEAOfficerAttributes>>, Object> sourceCallback)
      throws ZernovaException {

    executor = schedulerService.cpuLightScheduler(baseConfig.withShutdownTimeout(500, MILLISECONDS));
    sourceCallbakHandleTask = executor.scheduleAtFixedRate(() -> sourceCallback.handle(makeResult()), 0, 500, MILLISECONDS);
  }

  @Override
  public void onStop() {
    if (sourceCallbakHandleTask != null) {
      sourceCallbakHandleTask.cancel(false);
    }
    if (executor != null) {
      executor.stop();
    }
  }

  private Result<List<Result<String, DEAOfficerAttributes>>, Object> makeResult() {
    List<Result<String, DEAOfficerAttributes>> messages = new ArrayList<>(MESSAGES_PER_POLL);
    for (int i = 0; i < MESSAGES_PER_POLL; i++) {
      boolean isHank = random.nextInt() % 2 == 0;
      messages.add(Result.<String, DEAOfficerAttributes>builder()
          .output(MESSAGE_TEXT)
          .attributes(new DEAOfficerAttributes(isHank))
          .build());
    }

    return Result.<List<Result<String, DEAOfficerAttributes>>, Object>builder()
        .output(messages)
        .build();
  }
}
