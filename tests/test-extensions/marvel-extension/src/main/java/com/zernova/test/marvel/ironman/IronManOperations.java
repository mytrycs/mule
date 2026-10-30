/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.ironman;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.ThreadLocal.withInitial;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.zernova.runtime.api.meta.model.display.PathModel.Location.EMBEDDED;
import static com.zernova.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static com.zernova.runtime.core.api.util.UUID.getUUID;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.extension.api.annotation.execution.Execution;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.display.Path;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.sdk.api.annotation.param.display.ClassValue;
import com.zernova.test.marvel.model.Missile;
import com.zernova.test.marvel.model.Villain;

import java.util.concurrent.ScheduledExecutorService;

public class IronManOperations implements Initialisable, Disposable {

  public static final int MISSILE_TRAVEL_TIME = 200;
  public static final String FLIGHT_PLAN = "Go Straight";

  private static final ThreadLocal<String> taskTokenInThread = withInitial(IronManOperations::generateTaskToken);

  private ScheduledExecutorService executorService;

  @Override
  public void initialise() throws InitialisationException {
    executorService = newSingleThreadScheduledExecutor();
  }

  @Override
  public void dispose() {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  @MediaType(TEXT_PLAIN)
  public void fireMissile(@Config IronMan ironMan,
                          @Connection Missile missile,
                          Villain at,
                          com.zernova.sdk.api.runtime.process.CompletionCallback<String, Void> callback) {
    final Runnable launch = () -> {
      try {
        ironMan.track(missile);
        callback.success(com.zernova.sdk.api.runtime.operation.Result.<String, Void>builder()
            .output(missile.fireAt(at)).build());
      } catch (Exception e) {
        callback.error(e);
      }
    };

    // it takes the missile some time to reach target. Don't block while you kill
    executorService.schedule(launch, MISSILE_TRAVEL_TIME, MILLISECONDS);
  }

  @MediaType(TEXT_PLAIN)
  public void fireMissileMishap(@Config IronMan ironMan,
                                @com.zernova.sdk.api.annotation.param.Connection Missile missile,
                                Villain at,
                                CompletionCallback<String, Void> callback) {
    // A non blocking operation throwing an exception instead of calling callback#error.
    throw new IllegalStateException("Ultron jammed the missile system!");
  }

  @MediaType(TEXT_PLAIN)
  public void fireMissileEpicShot(@Config IronMan ironMan,
                                  @Connection Missile missile,
                                  Villain at,
                                  com.zernova.sdk.api.runtime.process.CompletionCallback<String, Void> callback) {
    // the last missile being fired in the movie has the hero looking at the missile going forward to its target. Doesn't look
    // epic if he's doing something else.
    try {
      ironMan.track(missile);
      callback.success(com.zernova.sdk.api.runtime.operation.Result.<String, Void>builder()
          .output(missile.fireAt(at)).build());
    } catch (Exception e) {
      callback.error(e);
    }
  }

  @MediaType(TEXT_PLAIN)
  public String findInstructions(@Optional @Path(acceptedFileExtensions = {"xml"}, location = EMBEDDED) String instructionsFile,
                                 @Optional @ClassValue(extendsOrImplements = {"com.starkindustries.Reader"}) String readerClass) {
    return instructionsFile;
  }

  @Execution(CPU_INTENSIVE)
  public void computeFlightPlan(@Config IronMan ironMan, CompletionCallback<Void, Void> callback) {
    final Runnable launch = () -> {
      callback.success(Result.<Void, Void>builder().build());
      ironMan.setFlightPlan(FLIGHT_PLAN);
    };

    // building a flight plan requires a lot of computation. Don't block while you kill
    executorService.schedule(launch, MISSILE_TRAVEL_TIME, MILLISECONDS);
  }

  /**
   * Verifies that a thread switch on the nested {@code interceptedChain} did occur.
   *
   * @param interceptedChain the chain in which a thread switch is expected.
   * @param callback         the {@link CompletionCallback}.
   */
  @MediaType(value = ANY, strict = false)
  public void assertResponseDifferentTask(Chain interceptedChain, CompletionCallback<Object, Object> callback) {
    String requestTaskToken = taskTokenInThread.get();

    interceptedChain.process(r -> {
      String responseTaskToken = taskTokenInThread.get();

      if (requestTaskToken.equals(responseTaskToken)) {
        final IllegalStateException e = new IllegalStateException(format("Response task (%s) was same as request task (%s)",
                                                                         responseTaskToken, requestTaskToken));
        callback.error(e);
      } else {
        callback.success(r);
      }
    }, (t, r) -> callback.error(t));
  }

  private static String generateTaskToken() {
    return currentThread().getName() + " - " + getUUID();
  }
}
