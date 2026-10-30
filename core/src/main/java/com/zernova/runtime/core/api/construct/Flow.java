/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.construct;

import static com.zernova.runtime.api.deployment.management.ComponentInitialStateManager.SERVICE_ID;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.execution.ExecutableComponent;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.construct.DefaultFlowBuilder;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;

import java.util.List;

import reactor.core.publisher.Flux;

/**
 * Defines a {@link Pipeline} that represents a Mule flow.
 * <p/>
 * A flow adds the following behaviour to a {@link Pipeline}
 * <ul>
 * <li>Rejects inbound events when Flow is not started</li>
 * <li>Gathers statistics and processing time data</li>
 * <li>Implements MessageProcessor allowing direct invocation of the pipeline</li>
 * <li>Supports the optional configuration of a {@link ProcessingStrategy} that determines how message processors are processed.
 * </ul>
 */
@NoImplement
public interface Flow extends ExecutableComponent, Lifecycle, Pipeline, Processor {

  String INITIAL_STATE_STOPPED = "stopped";
  String INITIAL_STATE_STARTED = "started";

  /**
   * @return initial state of the flow, which can be {@value INITIAL_STATE_STARTED} or {@value INITIAL_STATE_STOPPED}
   */
  String getInitialState();

  /**
   * @return whether this flow is started (ref: {@link Startable}).
   * @since 4.6 pushed up to the API interface.
   */
  boolean isStarted();

  /**
   * @return whether this flow is stopped (ref: {@link Stoppable}).
   * @since 4.6 pushed up to the API interface.
   */
  boolean isStopped();

  /**
   * Decorates the common flow {@link ReactiveProcessor} application by avoiding the emission of an error downstream in the
   * {@link Flux}. Normally used when invoking this {@link Flow} from a {@link Flux} chain, as in the {@code flow-ref} case.
   *
   * @return the decorated {@link ReactiveProcessor}
   */
  ReactiveProcessor referenced();

  /**
   * Creates a new flow builder
   *
   * @param name        name of the flow to be created. Non empty.
   * @param muleContext context where the flow will be registered. Non null.
   */
  static Builder builder(String name, ZernovaContext muleContext) {
    return new DefaultFlowBuilder(name, muleContext, ((DefaultZernovaContext) muleContext).getRegistry().lookupObject(SERVICE_ID));
  }

  interface Builder {

    /**
     * Configures the message source for the flow.
     *
     * @param messageSource message source to use. Non null.
     * @return same builder instance.
     */
    Builder source(MessageSource messageSource);

    /**
     * Configures the message processors to execute as part of flow.
     *
     * @param processors message processors to execute. Non null.
     * @return same builder instance.
     */
    Builder processors(List<Processor> processors);

    /**
     * Configures the message processors to execute as part of flow.
     *
     * @param processors message processors to execute.
     * @return same builder instance.
     */
    Builder processors(Processor... processors);

    /**
     * Configures the exception listener to manage exceptions thrown on the flow execution.
     *
     * @param exceptionListener exception listener to use on the flow. Non null.
     * @return same builder instance
     */
    Builder messagingExceptionHandler(FlowExceptionHandler exceptionListener);

    /**
     * Configures the factory used to create processing strategies on the created flow.
     *
     * @param processingStrategyFactory factory to create processing strategies. Non null.
     * @return same builder instance.
     */
    Builder processingStrategyFactory(ProcessingStrategyFactory processingStrategyFactory);

    /**
     * Configures a {@link com.zernova.runtime.core.internal.processor.strategy.DirectProcessingStrategyFactory} as the processing
     * strategy on the flow.
     *
     * @return same builder instance.
     */
    Builder withDirectProcessingStrategyFactory();

    /**
     * Usually a flow is started automatically ("started"), but this attribute can be used to disable initial startup ("stopped").
     *
     * @param initialState The initial state of the flow. Non null.
     * @return same builder instance.
     */
    Builder initialState(String initialState);

    /**
     * Configures the maximum permitted concurrency of the {@link Flow}. This value determines the maximum level of parallelism
     * that the Flow can use to optimize for performance when processing messages. Note that this does not impact in any way the
     * number of threads that a {@link MessageSource} may use to invoke a {@link Flow} and so if a direct or blocking
     * {@link ProcessingStrategy} is used where processing occurs in source threads it is actually the source that defines maximum
     * concurrency.
     *
     * @param maxConcurrency
     * @return an {@link Builder}
     */
    Builder maxConcurrency(int maxConcurrency);

    /**
     * Builds a flow with the provided configuration.
     *
     * @return a new flow instance.
     */
    Flow build();

  }
}
