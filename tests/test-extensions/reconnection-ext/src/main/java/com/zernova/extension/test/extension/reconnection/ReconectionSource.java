/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;

@MediaType("text/plain")
public class ReconectionSource extends Source<ReconnectableConnection, Void> {

  @Connection
  ConnectionProvider<ReconnectableConnection> connectionProvider;

  @Inject
  SchedulerService schedulerService;

  private final AtomicReference<ScheduledFuture<?>> scheduleWithFixedDelay = new AtomicReference<>();

  private Scheduler scheduler;

  @Override
  public void onStart(SourceCallback<ReconnectableConnection, Void> sourceCallback) throws ZernovaException {
    ReconnectableConnection connection = connectionProvider.connect();
    scheduler = schedulerService.ioScheduler();
    scheduleWithFixedDelay.set(scheduler.scheduleWithFixedDelay(() -> {
      if (ReconnectableConnectionProvider.fail) {
        sourceCallback.onConnectionException(new ConnectionException(new RuntimeException(), connection));
        scheduleWithFixedDelay.get().cancel(true);
      } else {
        sourceCallback.handle(Result.<ReconnectableConnection, Void>builder().output(connection).build());
      }
    }, 0, 1000, MILLISECONDS));

  }

  @Override
  public void onStop() {
    if (scheduler != null) {
      scheduleWithFixedDelay.get().cancel(true);
      scheduler.stop();
    }
  }
}
