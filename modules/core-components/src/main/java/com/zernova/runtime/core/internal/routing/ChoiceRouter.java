/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.management.stats.RouterStatistics.TYPE_OUTBOUND;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;

import static java.lang.String.format;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.management.stats.RouterStatistics;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.routing.result.RouterStatisticsRecorder;
import com.zernova.runtime.core.privileged.processor.Router;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;

/**
 * Routes the event to a single<code>MessageProcessor</code> using an expression to evaluate the event being processed and find
 * the first route that can be used.
 * <p>
 * If a default route has been configured and no match has been found, the default route will be used. Otherwise it continues the
 * execution through the next MP in the chain.
 */
public class ChoiceRouter extends AbstractComponent implements Router, RouterStatisticsRecorder, Lifecycle, ZernovaContextAware {

  public static final String ROUTE_SPAN_NAME_SUFFIX = ":route";
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final List<ProcessorRoute> routes = new ArrayList<>();

  private Processor defaultProcessor;
  private RouterStatistics routerStatistics;
  private ZernovaContext muleContext;
  private ExpressionManager expressionManager;
  private ComponentTracerFactory componentTracerFactory;

  public ChoiceRouter(ComponentTracerFactory componentTracerFactory) {
    routerStatistics = new RouterStatistics(TYPE_OUTBOUND);
    this.componentTracerFactory = componentTracerFactory;
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

  @Inject
  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  @Override
  public void initialise() throws InitialisationException {
    if (defaultProcessor == null) {
      defaultProcessor = event -> event;
    }
    routes.add(new ProcessorRoute(defaultProcessor, componentTracerFactory));

    for (ProcessorRoute route : routes) {
      route.setComponentTracer(componentTracerFactory.fromComponent(this, ROUTE_SPAN_NAME_SUFFIX));
      initialiseIfNeeded(route, muleContext);
    }
  }

  @Override
  public void start() throws ZernovaException {
    for (ProcessorRoute route : routes) {
      route.start();
    }

    started.set(true);
  }

  @Override
  public void stop() throws ZernovaException {
    for (ProcessorRoute route : routes) {
      route.stop();
    }

    started.set(false);
  }

  @Override
  public void dispose() {
    for (ProcessorRoute route : routes) {
      route.dispose();
    }
  }

  public void addRoute(final String expression, final Processor processor) {
    routes.add(new ProcessorExpressionRoute(expression, processor, componentTracerFactory));
  }

  public void setDefaultRoute(final Processor processor) {
    defaultProcessor = processor;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    return processToApply(event, this);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return Flux.merge(new SinkRouter(publisher, routes).collectPublishers());
  }

  public void updateStatistics(Processor processor) {
    if (getRouterStatistics() != null && getRouterStatistics().isEnabled()) {
      getRouterStatistics().incrementRoutedMessage(processor);
    }
  }

  public RouterStatistics getRouterStatistics() {
    return routerStatistics;
  }

  @Override
  public void setRouterStatistics(RouterStatistics routerStatistics) {
    this.routerStatistics = routerStatistics;
  }

  @Override
  public String toString() {
    return format("%s [flow=%s, started=%s]", getClass().getSimpleName(), getLocation().getRootContainerName(), started);
  }

  private class SinkRouter extends AbstractSinkRouter {

    SinkRouter(Publisher<CoreEvent> publisher, List<ProcessorRoute> routes) {
      super(publisher, routes, componentTracerFactory);
    }

    /**
     * Decides which route should execute for an incoming event and executes it, purposely separating those actions so that a
     * single {@link ExpressionManagerSession} can be used for all routes decision process.
     *
     * @param event the incoming event
     */
    @Override
    protected void route(CoreEvent event) {
      ExecutableRoute selectedRoute;
      try (ExpressionManagerSession session = expressionManager.openSession(getLocation(), event, NULL_BINDING_CONTEXT)) {
        selectedRoute = getRoutes().stream().filter(route -> route.shouldExecute(session)).findFirst().get();
      }
      selectedRoute.execute(event);
      updateStatistics(selectedRoute.getProcessor());
    }

  }
}
