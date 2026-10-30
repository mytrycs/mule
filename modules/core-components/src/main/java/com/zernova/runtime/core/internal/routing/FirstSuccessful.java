/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.Router;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;

/**
 * FirstSuccessful routes an event to the first target route that can accept it without throwing or returning an exception. If no
 * such route can be found, an exception is thrown. Note that this works more reliable with synchronous targets, but no such
 * restriction is imposed.
 */
public class FirstSuccessful extends AbstractComponent implements Router, Lifecycle, ZernovaContextAware {

  public static final String FIRST_SUCCESSFUL_ATTEMPT_SPAN_NAME_SUFFIX = ":attempt:";
  private final List<ProcessorRoute> routes = new ArrayList<>();
  private ZernovaContext muleContext;

  @Inject
  private ComponentTracerFactory componentTracerFactory;

  @Override
  public void initialise() throws InitialisationException {
    Long routeNumber = 1L;
    for (ProcessorRoute route : routes) {
      route.setMessagingExceptionHandler(null);
      route.setComponentTracer(componentTracerFactory.fromComponent(this,
                                                                    FIRST_SUCCESSFUL_ATTEMPT_SPAN_NAME_SUFFIX + routeNumber));
      initialiseIfNeeded(route, muleContext);
      routeNumber++;
    }
  }

  @Override
  public void start() throws ZernovaException {
    for (ProcessorRoute route : routes) {
      route.start();
    }
  }

  @Override
  public void stop() throws ZernovaException {
    for (ProcessorRoute route : routes) {
      route.stop();
    }
  }

  @Override
  public void dispose() {
    for (ProcessorRoute route : routes) {
      route.dispose();
    }
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    return processToApply(event, this);
  }

  public void addRoute(final Processor processor) {
    routes.add(new ProcessorRoute(processor, componentTracerFactory));
  }

  public void setRoutes(Collection<Processor> routes) {
    routes.forEach(this::addRoute);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return new FirstSuccessfulRouter(this, publisher, routes).getDownstreamPublisher();
  }


}
