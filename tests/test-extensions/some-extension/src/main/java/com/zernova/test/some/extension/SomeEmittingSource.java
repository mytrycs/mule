/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.some.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.slf4j.Logger;

@MediaType(TEXT_PLAIN)
public class SomeEmittingSource extends Source<byte[], Object> implements Initialisable, Disposable {

  private final Logger LOGGER = getLogger(SomeEmittingSource.class);

  @Inject
  private SchedulerService schedulerService;

  private static AtomicInteger emissions = new AtomicInteger(0);
  private Future<?> launchedEmitterFuture;

  @Parameter
  String message;

  @Parameter
  Integer times;
  private Scheduler emitterScheduler;

  @Override
  public void onStart(SourceCallback<byte[], Object> sourceCallback) {
    emissions.set(times);
    launchedEmitterFuture = emitterScheduler
        .submit(() -> {
          while (!Thread.currentThread().isInterrupted() && emissions.getAndDecrement() > 0) {
            LOGGER.info("Emitting an event through flow");
            sourceCallback.handle(Result.<byte[], Object>builder().output(message.getBytes()).build());
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              LOGGER.error("Emitting thread was interrupted: ", e);
            }
          }
        });
  }

  @Override
  public void onStop() {
    launchedEmitterFuture.cancel(true);
  }

  @Override
  public void initialise() {
    emitterScheduler = schedulerService.customScheduler(SchedulerConfig.config()
        .withName("Value emitting scheduler")
        .withMaxConcurrentTasks(1));
  }

  @Override
  public void dispose() {
    emitterScheduler.stop();
  }
}
