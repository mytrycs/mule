/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.construct;

import static java.util.Optional.ofNullable;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getDefaultProcessingStrategyFactory;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.execution.ExecutionResult;
import com.zernova.runtime.api.component.execution.InputEvent;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

/**
 * Default {@link Operation} implementation
 *
 * @since 4.5.0
 */
public class ZernovaOperation extends AbstractComponent implements Operation {

  private final static Logger LOGGER = getLogger(ZernovaOperation.class);

  public static Builder builder() {
    return new DefaultOperationBuilder();
  }

  private final List<Processor> processors;
  private final OperationModel operationModel;
  private final ZernovaContext muleContext;

  private MessageProcessorChain chain;
  private ProcessingStrategy processingStrategy;

  ZernovaOperation(List<Processor> processors,
                OperationModel operationModel,
                ZernovaContext muleContext) {
    this.processors = processors;
    this.operationModel = operationModel;
    this.muleContext = muleContext;
  }

  @Override
  public CompletableFuture<ExecutionResult> execute(InputEvent inputEvent) {
    return chain.execute(inputEvent);
  }

  @Override
  public CompletableFuture<Event> execute(Event event) {
    return chain.execute(event);
  }

  @Override
  public void initialise() throws InitialisationException {
    processingStrategy = getDefaultProcessingStrategyFactory(muleContext).create(muleContext, operationModel.getName());
    initialiseIfNeeded(processingStrategy);

    chain = newChain(ofNullable(processingStrategy), processors);
    initialiseIfNeeded(chain, true, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(processingStrategy);
    startIfNeeded(chain);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(chain);
    stopIfNeeded(processingStrategy);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(chain, LOGGER);
    disposeIfNeeded(processingStrategy, LOGGER);
  }

  @Override
  public OperationModel getModel() {
    return operationModel;
  }
}
