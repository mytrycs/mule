/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.core.api.config.i18n.CoreMessages.cannotCopyStreamPayload;
import static com.zernova.runtime.core.internal.routing.ForkJoinStrategy.RoutingPair.of;
import static com.zernova.runtime.core.internal.routing.RoutingUtils.setSourcePolicyChildContext;

import static java.util.Collections.emptyList;

import static reactor.core.publisher.Flux.fromIterable;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.routing.forkjoin.CollectMapForkJoinStrategyFactory;
import com.zernova.runtime.core.privileged.processor.Router;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.core.privileged.profiling.tracing.ComponentTracerAware;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;

/**
 * <p>
 * The <code>Scatter-Gather</code> router will broadcast copies of the current message to every route in parallel subject to any
 * limitation in concurrency that has been configured
 * <p>
 * For advanced use cases, a custom {@link ForkJoinStrategyFactory} can be applied to customize the logic used to aggregate the
 * route responses back into one single Event.
 * <p>
 * <b>EIP Reference:</b> <a href="http://www.eaipatterns.com/BroadcastAggregate.html"<a/>
 * </p>
 *
 * @since 3.5.0
 */
public class ScatterGatherRouter extends AbstractForkJoinRouter implements Router {

  public static final String SCATTER_GATHER_ROUTE_SPAN_NAME_SUFFIX = ":route";
  private List<MessageProcessorChain> routes = emptyList();

  @Inject
  private ComponentTracerFactory<CoreEvent> componentTracerFactory;

  @Inject
  private FeatureFlaggingService featureFlaggingService;

  @Override
  protected Consumer<CoreEvent> onEvent() {
    return event -> validateMessageIsNotConsumable(event.getMessage());
  }

  @Override
  protected Publisher<ForkJoinStrategy.RoutingPair> getRoutingPairs(CoreEvent event) {
    return fromIterable(routes).map(route -> of(eventForRoute(event), route));
  }

  private CoreEvent eventForRoute(CoreEvent event) {
    if (!(event instanceof InternalEvent)) {
      return event;
    }
    InternalEvent copy = (InternalEvent) CoreEvent.builder(event).message(event.getMessage()).build();
    setSourcePolicyChildContext(copy, featureFlaggingService);
    return copy;
  }

  @Override
  protected List<MessageProcessorChain> getOwnedObjects() {
    return routes;
  }

  public void setRoutes(List<MessageProcessorChain> routes) {
    this.routes = routes;
    for (MessageProcessorChain route : routes) {
      if (route instanceof ComponentTracerAware) {
        ((ComponentTracerAware) route)
            .setComponentTracer(componentTracerFactory.fromComponent(this, SCATTER_GATHER_ROUTE_SPAN_NAME_SUFFIX));
      }
    }
  }

  @Override
  protected boolean isDelayErrors() {
    return true;
  }

  @Override
  protected int getDefaultMaxConcurrency() {
    return routes.size();
  }

  @Override
  protected ForkJoinStrategyFactory getDefaultForkJoinStrategyFactory() {
    return new CollectMapForkJoinStrategyFactory(featureFlaggingService);
  }

  /**
   * Validates that the payload is not consumable so it can be copied.
   * <p>
   * If validation fails then throws a MessagingException
   *
   * @param message
   * @throws ZernovaException if the payload is consumable
   */
  public static void validateMessageIsNotConsumable(Message message) {
    if (message.getPayload().getDataType().isStreamType()) {
      throw new ZernovaRuntimeException(cannotCopyStreamPayload(message.getPayload().getDataType().getType().getName()));
    }
  }
}
