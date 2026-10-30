/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy;

import static com.zernova.runtime.core.privileged.processor.MessageProcessors.WITHIN_PROCESS_TO_APPLY;

import static reactor.core.publisher.Mono.just;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.construct.BackPressureReason;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.api.processor.Sink;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import reactor.core.publisher.Mono;

/**
 * {@link Sink} implementation that uses a {@link Mono} for each request
 */
public class StreamPerEventSink implements Sink {

  private final ReactiveProcessor processor;
  private final Consumer<CoreEvent> eventConsumer;

  /**
   * Creates a {@link StreamPerEventSink}.
   *
   * @param processor     the processor to process events emitted onto stream, typically this processor will represent the flow
   *                      pipeline.
   * @param eventConsumer event consumer called just before {@link CoreEvent}'s emission.
   */
  public StreamPerEventSink(ReactiveProcessor processor, Consumer<CoreEvent> eventConsumer) {
    this.processor = processor;
    this.eventConsumer = eventConsumer;
  }

  @Override
  public void accept(CoreEvent event) {
    AtomicReference<Throwable> exception = new AtomicReference<>();
    just(event)
        .doOnNext(request -> eventConsumer.accept(request))
        .transform(processor)
        // Make inner chains behave correctly in the context of this mono
        .contextWrite(ctx -> ctx.put(WITHIN_PROCESS_TO_APPLY, true))
        .subscribe(null, exception::set);
    if (exception.get() != null) {
      if (exception.get() instanceof RuntimeException) {
        throw (RuntimeException) exception.get();
      } else {
        throw new ZernovaRuntimeException(exception.get());
      }
    }
  }

  @Override
  public BackPressureReason emit(CoreEvent event) {
    accept(event);
    return null;
  }
}
