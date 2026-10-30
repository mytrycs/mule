/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.PARALLEL_FOREACH_FLATTEN_MESSAGE;
import static com.zernova.runtime.core.api.processor.strategy.AsyncProcessingStrategyFactory.DEFAULT_MAX_CONCURRENCY;
import static com.zernova.runtime.core.api.util.StreamingUtils.updateTypedValueForStreaming;
import static com.zernova.runtime.core.internal.routing.RoutingUtils.setSourcePolicyChildContext;
import static com.zernova.runtime.core.internal.routing.split.ExpressionSplittingStrategy.DEFAULT_SPLIT_EXPRESSION;
import static com.zernova.runtime.core.internal.routing.ForeachUtils.manageTypedValueForStreaming;
import static com.zernova.runtime.core.internal.routing.ForkJoinStrategy.RoutingPair.of;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.buildNewChainWithListOfProcessors;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;

import static reactor.core.publisher.Flux.fromIterable;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.routing.forkjoin.CollectListForkJoinStrategyFactory;
import com.zernova.runtime.core.internal.routing.split.SplittingStrategy;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;
import com.zernova.runtime.core.internal.routing.split.ExpressionSplittingStrategy;

import java.util.Iterator;
import java.util.List;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;

/**
 * <p>
 * The <code>Parallel-Foreach</code> scope splits the incoming {@link com.zernova.runtime.api.message.Message} into n parts will
 * broadcast copies of the current message to every route in parallel subject to any limitation in concurrency that has been
 * configured
 * <p>
 * For advanced use cases, a custom {@link ForkJoinStrategyFactory} can be applied to customize the logic used to aggregate the
 * route responses back into one single Event.
 * <p>
 * <b>EIP Reference:</b> <a href="http://www.eaipatterns.com/BroadcastAggregate.html"<a/>
 * </p>
 *
 * @since 4.2.0
 */
public class ParallelForEach extends AbstractForkJoinRouter {

  public static final String PARALLEL_FOREACH_ITERATION_SPAN_NAME_SUFFIX = ":iteration";
  @Inject
  protected ExpressionManager expressionManager;

  @Inject
  protected StreamingManager streamingManager;

  @Inject
  protected FeatureFlaggingService featureFlaggingService;

  @Inject
  private ComponentTracerFactory componentTracerFactory;

  private String collectionExpression = DEFAULT_SPLIT_EXPRESSION;
  private SplittingStrategy<CoreEvent, Iterator<TypedValue<?>>> splittingStrategy;

  private List<Processor> messageProcessors;
  private MessageProcessorChain nestedChain;

  @Override
  public void initialise() throws InitialisationException {
    nestedChain =
        buildNewChainWithListOfProcessors(of(resolveProcessingStrategy()), messageProcessors,
                                          componentTracerFactory.fromComponent(this,
                                                                               PARALLEL_FOREACH_ITERATION_SPAN_NAME_SUFFIX));
    splittingStrategy = new ExpressionSplittingStrategy(expressionManager, collectionExpression);
    super.initialise();
  }

  @Override
  protected Publisher<ForkJoinStrategy.RoutingPair> getRoutingPairs(CoreEvent event) {
    return fromIterable(() -> splittingStrategy.split(event))
        .map(partTypedValue -> CoreEvent.builder(event)
            .message(createMessage(partTypedValue, event))
            .build())
        .cast(InternalEvent.class)
        .doOnNext(evt -> setSourcePolicyChildContext(evt, featureFlaggingService))
        .map(partEvent -> of(partEvent, nestedChain));
  }

  protected TypedValue manageTypedValuePayload(TypedValue partTypedValue, CoreEvent event) {
    if (featureFlaggingService.isEnabled(PARALLEL_FOREACH_FLATTEN_MESSAGE)) {
      return manageTypedValueForStreaming(partTypedValue, event, streamingManager);
    } else {
      return updateTypedValueForStreaming(partTypedValue, event, streamingManager);
    }
  }

  @Override
  protected List<MessageProcessorChain> getOwnedObjects() {
    return singletonList(nestedChain);
  }

  public void setMessageProcessors(List<Processor> messageProcessors) {
    this.messageProcessors = messageProcessors;
  }

  @Override
  protected boolean isDelayErrors() {
    return true;
  }

  @Override
  protected int getDefaultMaxConcurrency() {
    return DEFAULT_MAX_CONCURRENCY;
  }

  @Override
  protected ForkJoinStrategyFactory getDefaultForkJoinStrategyFactory() {
    return new CollectListForkJoinStrategyFactory(false, featureFlaggingService);
  }

  /**
   * Set the expression used to split the incoming message.
   *
   * @param collectionExpression
   */
  public void setCollectionExpression(String collectionExpression) {
    this.collectionExpression = collectionExpression;
  }

  private Message createMessage(TypedValue<?> partTypedValue, CoreEvent event) {
    if (featureFlaggingService.isEnabled(PARALLEL_FOREACH_FLATTEN_MESSAGE) && partTypedValue.getValue() instanceof Message) {
      Message message = (Message) partTypedValue.getValue();
      return Message.builder(message).payload(manageTypedValuePayload(partTypedValue, event)).build();
    } else {
      return Message.builder()
          .payload(manageTypedValuePayload(partTypedValue, event))
          .build();
    }
  }
}
