/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChildContext;
import static com.zernova.tck.junit4.AbstractZernovaContextTestCase.getAppleFlowComponentLocationAnnotations;
import static com.zernova.tck.util.ZernovaContextUtils.eventBuilder;
import static org.openjdk.jmh.annotations.Mode.SingleShotTime;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.AbstractBenchmark;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.slf4j.Logger;
import com.zernova.runtime.core.internal.routing.Foreach;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Warmup(iterations = 2)
@Measurement(iterations = 50)
@OutputTimeUnit(NANOSECONDS)
public class ForeachBenchmark extends AbstractBenchmark {

  private static final Logger LOGGER = getLogger(ForeachBenchmark.class);
  private static final int PAYLOADS = 50000;
  private static final int NESTED_PAYLOADS = 5000;
  private static final int INNER_PAYLOADS = 100;

  private static final int CONCURRENCY = 10;
  private static final int PAYLOADS_CONCURRENT = 50000;
  private static final int CONCURRENCY_TIMEOUT_SECONDS = 30;

  private ConfigurationComponentLocator configurationComponentLocator;

  private ZernovaContext muleContext;
  private CoreEvent singleForeachEvent;
  private CoreEvent nestedForeachEvent;

  private CoreEvent foreachEvent;
  private List<String> chainedPayloads;

  @Setup(Level.Trial)
  public void setup() throws Exception {
    configurationComponentLocator = mock(ConfigurationComponentLocator.class, RETURNS_DEEP_STUBS);
    muleContext = createMuleContextWithServices();
    muleContext.start();

    // Create single foreach event
    List<String> payload = new ArrayList<>();
    for (int i = 0; i < PAYLOADS; i++) {
      payload.add("" + i);
    }

    singleForeachEvent = eventBuilder(muleContext).message(of(payload)).build();

    // Create nested foreach event
    List<List<String>> nestedForeachPayload = new ArrayList<>();
    for (int i = 0; i < NESTED_PAYLOADS; i++) {
      List<String> items = new ArrayList<>();
      for (int j = 0; j < INNER_PAYLOADS; j++) {
        items.add(i + "_" + j);
      }
      nestedForeachPayload.add(items);
    }

    nestedForeachEvent = eventBuilder(muleContext).message(of(nestedForeachPayload)).build();

    // Create concurrency event
    foreachEvent = eventBuilder(muleContext).message(of(asList(1, 2, 3))).build();
    chainedPayloads = new ArrayList<>();
    for (int i = 0; i < PAYLOADS_CONCURRENT; i++) {
      chainedPayloads.add("" + i);
    }
  }

  private Foreach createForeach() {
    Foreach foreach = new Foreach();
    foreach.setAnnotations(getAppleFlowComponentLocationAnnotations());
    return foreach;
  }

  @TearDown
  public void tearDown() {
    muleContext.dispose();
  }

  @Benchmark
  @BenchmarkMode(SingleShotTime)
  public int singleForeach() {
    AtomicInteger counter = new AtomicInteger();
    try {
      Foreach singleForeach = createForeach();
      Processor capturedEventProcessor = event -> {
        counter.incrementAndGet();
        return event;
      };

      singleForeach.setMessageProcessors(singletonList(capturedEventProcessor));
      initialiseIfNeeded(singleForeach, muleContext);

      singleForeach.process(singleForeachEvent);
    } catch (Throwable t) {
      LOGGER.error("Unexpected error on singleForeach", t);
    }
    return counter.get();
  }

  @Benchmark
  @BenchmarkMode(SingleShotTime)
  public int nestedForeach() {
    AtomicInteger counter = new AtomicInteger();
    try {
      Foreach internalForeach = createForeach();
      Processor nestedEventProcessor = event -> {
        counter.incrementAndGet();
        return event;
      };

      internalForeach.setMessageProcessors(singletonList(nestedEventProcessor));
      Foreach nestedForeach = createForeach();
      nestedForeach.setMessageProcessors(singletonList(internalForeach));
      initialiseIfNeeded(nestedForeach, muleContext);

      nestedForeach.process(nestedForeachEvent);
    } catch (Throwable t) {
      LOGGER.error("Unexpected error on nestedForeach", t);
    }

    return counter.get();
  }

  @Benchmark
  @BenchmarkMode(SingleShotTime)
  public int multiplesThreadsUsingSameForeach() {
    AtomicInteger counter = new AtomicInteger();
    try {
      // Process 1st foreach
      Foreach foreach = createForeach();
      Processor emptyEventProcessor = event -> event;
      foreach.setMessageProcessors(singletonList(emptyEventProcessor));
      initialiseIfNeeded(foreach, muleContext);

      // Process 2nd foreach concurrently
      CountDownLatch threadsLatch = new CountDownLatch(CONCURRENCY);
      Latch mainThreadLatch = new Latch();

      // Create and initialize 2nd foreach
      Processor secondCapturedEventProcessor = event -> {
        counter.incrementAndGet();
        return event;
      };

      Foreach chainedForeach = createForeach();
      chainedForeach.setMessageProcessors(singletonList(secondCapturedEventProcessor));
      initialiseIfNeeded(chainedForeach, muleContext);

      CoreEvent parentEvent = foreach.process(foreachEvent);
      ExecutorService executorService = newFixedThreadPool(CONCURRENCY);
      for (int t = 0; t < CONCURRENCY; t++) {
        BaseEventContext childContext = newChildContext(parentEvent, Optional.empty());
        CoreEvent childEvent = CoreEvent.builder(childContext, parentEvent).message(of(chainedPayloads)).build();
        executorService.submit(() -> {
          try {
            threadsLatch.countDown();
            mainThreadLatch.await();
            // Run chainedForeach
            chainedForeach.process(childEvent);
          } catch (Throwable e) {
            LOGGER.error("An unexpected error processing events", e);
          }
        });
      }
      // Await all threads starts
      threadsLatch.await();
      mainThreadLatch.release();
      executorService.shutdown();
      executorService.awaitTermination(CONCURRENCY_TIMEOUT_SECONDS, SECONDS);
    } catch (Throwable t) {
      LOGGER.error("Unexpected error on multiplesThreadsUsingSameForeach", t);
    }
    return counter.get();
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    when(configurationComponentLocator.find(any(Location.class))).thenReturn(empty());
    when(configurationComponentLocator.find(any(ComponentIdentifier.class))).thenReturn(emptyList());

    return singletonMap(REGISTRY_KEY, configurationComponentLocator);
  }
}
