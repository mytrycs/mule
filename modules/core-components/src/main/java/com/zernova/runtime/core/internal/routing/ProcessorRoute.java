/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder.MessagingExceptionHandlerAware;

import com.zernova.runtime.core.privileged.profiling.tracing.ComponentTracerAware;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a simple route with the {@link Processor} it leads to.
 *
 * @since 4.3.0
 */
public class ProcessorRoute extends AbstractComponent implements ZernovaContextAware, Lifecycle {

  private final static Logger LOGGER = LoggerFactory.getLogger(ProcessorRoute.class);
  public static final String PROCESSOR_ROUTE_SPAN_NAME = "route";

  private final Processor processor;
  private ComponentTracer componentTracer;

  // just let the error be propagated to the outer chain...
  private FlowExceptionHandler messagingExceptionHandler = (exception, event) -> null;
  private ZernovaContext muleContext;

  public ProcessorRoute(Processor processor, ComponentTracerFactory componentTracerFactory) {
    requireNonNull(processor, "processor can't be null");
    this.processor = processor;
    this.componentTracer = componentTracerFactory.fromComponent(this, PROCESSOR_ROUTE_SPAN_NAME, "");
  }

  public Processor getProcessor() {
    return processor;
  }

  @Override
  public String toString() {
    return reflectionToString(this, SHORT_PREFIX_STYLE);
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
    if (processor instanceof ZernovaContextAware) {
      ((ZernovaContextAware) processor).setMuleContext(context);
    }
  }

  public void setMessagingExceptionHandler(FlowExceptionHandler messagingExceptionHandler) {
    this.messagingExceptionHandler = messagingExceptionHandler;
  }

  public void setComponentTracer(ComponentTracer componentTracer) {
    this.componentTracer = componentTracer;
  }

  @Override
  public void initialise() throws InitialisationException {
    if (processor instanceof MessagingExceptionHandlerAware) {
      ((MessagingExceptionHandlerAware) processor).setMessagingExceptionHandler(messagingExceptionHandler);
    }
    if (processor instanceof ComponentTracerAware) {
      ((ComponentTracerAware) processor)
          .setComponentTracer(componentTracer);
    }
    initialiseIfNeeded(processor, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(processor);
  }

  /**
   * @param session an {@link ExpressionManagerSession}
   * @return whether this route can be taken given the context
   */
  public boolean accepts(ExpressionManagerSession session) {
    return true;
  }

  /**
   * @return an {@link ExecutableRoute} for this one.
   */
  ExecutableRoute toExecutableRoute() {
    return new ExecutableRoute(this);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(processor);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(processor, LOGGER);
  }
}
