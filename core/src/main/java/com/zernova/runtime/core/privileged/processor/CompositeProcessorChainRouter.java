/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.setMuleContextIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.internal.event.DefaultEventContext.child;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processWithChildContext;
import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.from;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;

import java.util.List;
import java.util.function.BiFunction;

import org.slf4j.Logger;

/**
 * Component to be used that supports a collection of {@link MessageProcessorChain} and executes them in sequentially waiting for
 * each chain and all child context executions (such as async blocks) to complete.
 * <p/>
 * Meant to be used by runtime privileged extensions that need to construct top level chains of execution.
 *
 * @since 4.0
 */
public class CompositeProcessorChainRouter extends AbstractExecutableComponent implements Lifecycle {

  private static Logger LOGGER = getLogger(CompositeProcessorChainRouter.class);

  private List<MessageProcessorChain> processorChains = emptyList();

  public void setProcessorChains(List<MessageProcessorChain> processorChains) {
    this.processorChains = processorChains;
  }

  @Override
  protected ReactiveProcessor getExecutableFunction() {
    return publisher -> from(publisher)
        .flatMapMany(initial -> fromIterable(processorChainsToExecute(processorChains))
            .reduce(initial, processChain()));
  }

  private BiFunction<CoreEvent, ReactiveProcessor, CoreEvent> processChain() {
    return (event, processorChain) -> {
      Latch completionLatch = new Latch();
      BaseEventContext childContext = child((BaseEventContext) event.getContext(), ofNullable(getLocation()));
      childContext.onComplete((response, throwable) -> completionLatch.countDown());
      CoreEvent result = from(processWithChildContext(event, processorChain, childContext)).block();
      try {
        // Block until all child contexts are complete
        completionLatch.await();
      } catch (InterruptedException e) {
        throw new ZernovaRuntimeException(createStaticMessage("Thread interrupted waiting for child processor chain to complete.",
                                                           e));
      }
      return result;
    };
  }

  protected List<? extends ReactiveProcessor> processorChainsToExecute(List<MessageProcessorChain> processorChains) {
    return processorChains;
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(processorChains);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(processorChains, LOGGER);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(processorChains);
  }

  @Override
  public void setMuleContext(ZernovaContext muleContext) {
    super.setMuleContext(muleContext);
    setMuleContextIfNeeded(processorChains, muleContext);
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(processorChains, muleContext);
  }
}
