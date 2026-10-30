/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.el;

import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import static org.openjdk.jmh.annotations.Threads.MAX;

import com.zernova.AbstractBenchmark;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;

@OutputTimeUnit(NANOSECONDS)
@Threads(MAX)
public class ParseLogTemplateBenchmark extends AbstractBenchmark {

  private ExtendedExpressionManager expressionManager;
  private ZernovaContext muleContext;
  private CoreEvent event;

  @Setup
  public void setup() throws ZernovaException {
    muleContext = createMuleContextWithServices();
    expressionManager = muleContext.getExpressionManager();
    EventContext context = create(createFlow(muleContext), CONNECTOR_LOCATION);
    event = CoreEvent.builder(context).message(of(PAYLOAD)).build();
  }

  @TearDown
  public void teardown() throws ZernovaException {
    stopIfNeeded(muleContext.getSchedulerService());
    muleContext.dispose();
  }

  @Benchmark
  public Object parseLogTemplatePayload() {
    return expressionManager.parseLogTemplate("#[payload]", event, CONNECTOR_LOCATION, NULL_BINDING_CONTEXT);
  }

  @Benchmark
  public Object parseLogTemplatePayloadNested() {
    return expressionManager.parseLogTemplate("#['payload is: #[payload]']", event, CONNECTOR_LOCATION, NULL_BINDING_CONTEXT);
  }

}
