/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.util.StreamingUtils.updateTypedValueWithCursorProvider;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.core.privileged.event.EventedResult;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An implementation of {@link Chain} that wraps a {@link Processor} and allows to execute it
 *
 * @since 4.0
 */
public class ImmutableProcessorChainExecutor implements ProcessorChainExecutor {

  private final StreamingManager streamingManager;

  /**
   * Processor that will be executed upon calling process
   */
  private final MessageProcessorChain chain;

  /**
   * Event that will be cloned for dispatching
   */
  private final CoreEvent originalEvent;

  /**
   * Excutor to delegate the processing
   */
  private final ChainExecutor chainExecutor;

  /**
   * Creates a new immutable instance
   *
   * @param streamingManager
   * @param event            the original {@link CoreEvent} for the execution of the given chain
   * @param chain            a {@link Processor} chain to be executed
   */
  public ImmutableProcessorChainExecutor(StreamingManager streamingManager, CoreEvent event, MessageProcessorChain chain) {
    this.streamingManager = streamingManager;
    this.originalEvent = event;
    this.chain = chain;
    this.chainExecutor = new ChainExecutor(chain, originalEvent);
  }

  @Override
  public void process(Consumer<Result> onSuccess, BiConsumer<Throwable, Result> onError) {
    doProcess(originalEvent, onSuccess, onError);
  }

  @Override
  public void process(Object payload, Object attributes, Consumer<Result> onSuccess, BiConsumer<Throwable, Result> onError) {
    CoreEvent customEvent = CoreEvent.builder(originalEvent)
        .message(Message.builder()
            .payload(updateTypedValueWithCursorProvider(TypedValue.of(payload), streamingManager))
            .attributes(TypedValue.of(attributes))
            .build())
        .build();

    doProcess(customEvent, onSuccess, onError);
  }

  @Override
  public void process(Result result, Consumer<Result> onSuccess, BiConsumer<Throwable, Result> onError) {
    if (result instanceof EventedResult) {
      doProcess(((EventedResult) result).getEvent(), onSuccess, onError);
    } else {
      process(result.getOutput(), result.getAttributes(), onSuccess, onError);
    }
  }

  private void doProcess(CoreEvent event, Consumer<Result> onSuccess, BiConsumer<Throwable, Result> onError) {
    checkArgument(onSuccess != null,
                  "A success completion handler is required in order to execute the components chain, but it was null");
    checkArgument(onError != null,
                  "An error completion handler is required in order to execute the components chain, but it was null");
    chainExecutor.execute(event, onSuccess, onError);
  }

  @Override
  public List<Processor> getMessageProcessors() {
    return chain.getMessageProcessors();
  }

  @Override
  public CoreEvent getOriginalEvent() {
    return originalEvent;
  }

}
