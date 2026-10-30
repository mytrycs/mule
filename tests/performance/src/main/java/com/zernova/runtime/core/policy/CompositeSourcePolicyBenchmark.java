/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.policy;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.internal.execution.utils.SourcePolicyTestUtils.block;

import com.zernova.AbstractBenchmark;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.core.api.policy.PolicyChain;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.core.internal.policy.CompositeSourcePolicy;
import com.zernova.runtime.core.internal.policy.MessageSourceResponseParametersProcessor;
import com.zernova.runtime.core.internal.policy.SourcePolicy;
import com.zernova.runtime.core.internal.policy.SourcePolicyFailureResult;
import com.zernova.runtime.core.internal.policy.SourcePolicySuccessResult;

import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Threads;
import org.reactivestreams.Publisher;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(MICROSECONDS)
public class CompositeSourcePolicyBenchmark extends AbstractBenchmark {

  private SourcePolicy handler;
  private MessageSourceResponseParametersProcessor sourceRpp;

  @Setup(Level.Trial)
  public void setUp() {
    handler = new CompositeSourcePolicy(asList(new Policy(new PolicyChain() {

      @Override
      public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
        return publisher;
      }
    }, "")), eventPub -> eventPub, empty(), (policy, nextProcessor) -> nextProcessor, null);

    sourceRpp = new MessageSourceResponseParametersProcessor() {

      @Override
      public CheckedFunction<CoreEvent, Map<String, Object>> getSuccessfulExecutionResponseParametersFunction() {
        return event -> emptyMap();
      }

      @Override
      public CheckedFunction<CoreEvent, Map<String, Object>> getFailedExecutionResponseParametersFunction() {
        return event -> emptyMap();
      }
    };
  }

  @Benchmark
  @Threads(Threads.MAX)
  public Either<SourcePolicyFailureResult, SourcePolicySuccessResult> source() throws Throwable {
    CoreEvent event;
    Message.Builder messageBuilder = Message.builder().value(PAYLOAD);
    CoreEvent.Builder eventBuilder =
        CoreEvent.builder(create("", "", CONNECTOR_LOCATION, null, empty())).message(messageBuilder.build());
    event = eventBuilder.build();

    return block(callback -> handler.process(event, sourceRpp, callback));
  }

}
