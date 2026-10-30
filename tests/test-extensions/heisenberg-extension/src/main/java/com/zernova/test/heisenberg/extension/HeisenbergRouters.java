/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.ONCE;
import static com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.ONCE_OR_NONE;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toMap;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback;
import com.zernova.runtime.extension.api.runtime.process.VoidCompletionCallback;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.sdk.api.annotation.deprecated.Deprecated;
import com.zernova.sdk.api.annotation.param.display.Summary;
import com.zernova.sdk.api.annotation.route.ExecutionOccurrence;
import com.zernova.test.heisenberg.extension.model.Attribute;
import com.zernova.test.heisenberg.extension.route.AfterCall;
import com.zernova.test.heisenberg.extension.route.BeforeCall;
import com.zernova.test.heisenberg.extension.route.DrugKillingRoute;
import com.zernova.test.heisenberg.extension.route.KillingRoute;
import com.zernova.test.heisenberg.extension.route.OtherwiseRoute;
import com.zernova.test.heisenberg.extension.route.WhenRoute;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HeisenbergRouters implements Initialisable, Startable, Stoppable, Disposable {

  public static final String NOT_INITIALISED = "not_initialised";
  private String state = NOT_INITIALISED;

  public void voidRouter(WhenRoute when, VoidCompletionCallback callback) {
    when.getChain()
        .process(state, null, r -> callback.success(), (e, r) -> callback.error(e));
  }

  public void sdkVoidRouter(WhenRoute when, com.zernova.sdk.api.runtime.process.VoidCompletionCallback callback) {
    when.getChain()
        .process(state, null, r -> callback.success(), (e, r) -> callback.error(e));
  }

  @Parameter
  @Optional(defaultValue = "0")
  private int fieldParam;

  public void concurrentRouteExecutor(WhenRoute when, com.zernova.sdk.api.runtime.process.RouterCompletionCallback callback) {
    Consumer<Chain> processor = (chain) -> {
      final Latch latch = new Latch();
      chain.process((result -> latch.release()), (error, result) -> latch.release());
      try {
        latch.await(10000, MILLISECONDS);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    Thread first = new Thread(() -> processor.accept(when.getChain()));
    Thread second = new Thread(() -> processor.accept(when.getChain()));
    first.start();
    second.start();
    try {
      first.join();
      second.join();
    } catch (Exception e) {
      callback.error(e);
    }
    callback.success(com.zernova.sdk.api.runtime.operation.Result.builder().output("SUCCESS").build());
  }

  @Deprecated(message = "Simple routers are too simple, we will no longer suport them.", since = "1.2.2")
  public void simpleRouter(WhenRoute when, RouterCompletionCallback callback) {
    when.getChain()
        .process(callback::success,
                 (e, r) -> callback.error(e));
  }

  public void twoRoutesRouter(String processorName,
                              @ExecutionOccurrence(ONCE_OR_NONE) WhenRoute when,
                              @com.zernova.sdk.api.annotation.param.Optional OtherwiseRoute other,
                              RouterCompletionCallback callback) {
    if (when.shouldExecute()) {
      when.getChain().process(Result.builder()
          .output(processorName)
          .attributes(when.getMessage()).build(),
                              callback::success, (e, r) -> callback.error(e));
    } else if (other != null && other.shouldExecute()) {
      other.getChain().process(processorName, null, callback::success, (e, r) -> callback.error(e));
    } else {
      callback.error(new IllegalArgumentException("No route executed"));
    }
  }

  public void stereotypedRoutes(KillingRoute killingRoute,
                                @Optional DrugKillingRoute drugKillingRoute,
                                com.zernova.sdk.api.runtime.process.RouterCompletionCallback callback) {
    killingRoute.getChain().process(result -> {
      if (drugKillingRoute != null) {
        drugKillingRoute.getChain().process(result,
                                            r -> callback.success(com.zernova.sdk.api.runtime.operation.Result.builder()
                                                .output(r.getOutput()).attributes(r.getAttributes()).build()),
                                            (e, r) -> callback.error(e));
      } else {
        callback.success(com.zernova.sdk.api.runtime.operation.Result.builder().output(result.getOutput())
            .attributes(result.getAttributes()).build());
      }
    }, (e, r) -> callback.error(e));
  }

  @Summary("Allows to take actions over the event before and after the execution of a processor")
  public void spy(String processor,
                  @Optional @NullSafe @Expression(NOT_SUPPORTED) List<Attribute> withAttributes,
                  @Optional @ExecutionOccurrence(ONCE) BeforeCall beforeCallAssertions,
                  @Optional @ExecutionOccurrence(ONCE) AfterCall afterCallAssertions,
                  com.zernova.sdk.api.runtime.process.RouterCompletionCallback callback) {
    Map<String, Object> attr = withAttributes.stream().collect(toMap(Attribute::getName, r -> r));

    if (beforeCallAssertions != null) {
      beforeCallAssertions.getChain()
          // Control de payload/attributes of the chain execution
          .process(attr, null,
                   success -> {
                     // execute the element being spied
                     System.out.println(processor);
                     // and then execute afterAssertions using the result of that MP,
                     // in this case we just pipe the previous "success"
                     if (afterCallAssertions != null) {
                       afterCallAssertions.getChain()
                           // Complete the execution of the router
                           .process(success, callback::success, (t, e) -> callback.error(t));
                     } else {
                       callback.success(success);
                     }
                   },
                   (error, lastResult) -> callback.error(error));
    } else if (afterCallAssertions != null) {
      afterCallAssertions.getChain()
          .process(attr, null, callback::success, (t, e) -> callback.error(t));
    } else {
      callback.success(com.zernova.sdk.api.runtime.operation.Result.builder().build());
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    checkState(NOT_INITIALISED);
    state = Initialisable.PHASE_NAME;
  }

  @Override
  public void start() throws ZernovaException {
    checkState(Initialisable.PHASE_NAME);
    state = Startable.PHASE_NAME;
  }

  @Override
  public void stop() throws ZernovaException {
    checkState(Startable.PHASE_NAME);
    state = Stoppable.PHASE_NAME;
  }

  @Override
  public void dispose() {
    try {
      checkState(Stoppable.PHASE_NAME);
    } catch (InitialisationException e) {
      throw new RuntimeException(e);
    }
    state = Disposable.PHASE_NAME;
  }

  private void checkState(String expected) throws InitialisationException {
    if (!state.equals(expected)) {
      throw new InitialisationException(
                                        createStaticMessage("Invalid state: expected %s but as %s", expected, state), this);
    }
  }

  public int routerField(int expected, int newValue) {
    checkArgument(expected == fieldParam, "Expected " + expected + " but was " + fieldParam);
    fieldParam = newValue;
    return fieldParam;
  }

}
