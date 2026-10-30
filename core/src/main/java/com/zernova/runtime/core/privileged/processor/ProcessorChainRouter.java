/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import static java.util.Collections.emptyList;

import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Mono.from;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;

import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Component to be used that supports a collection of processors and executes them in order.
 * <p/>
 * Meant to be used be runtime privileged extensions that needs to construct top level chains of execution.
 *
 * @since 4.0
 */
public class ProcessorChainRouter extends AbstractExecutableComponent implements Lifecycle {

  private static Logger LOGGER = getLogger(ProcessorChainRouter.class);

  @Inject
  private ZernovaContext muleContext;

  private String name;
  private List<Processor> processors = emptyList();
  private MessageProcessorChain processorChain;

  @Override
  protected ReactiveProcessor getExecutableFunction() {
    return publisher -> from(publisher).transform(processorChain);
  }

  public void setProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(processorChain);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(processorChain, LOGGER);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(processorChain);
  }

  @Override
  public void initialise() throws InitialisationException {
    DefaultMessageProcessorChainBuilder builder = new DefaultMessageProcessorChainBuilder();
    builder.setName("processor chain '" + name + "'");
    builder.chain(processors);
    processorChain = builder.build();
    initialiseIfNeeded(processorChain, muleContext);
  }
}

