/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;
import static reactor.core.Exceptions.propagate;
import static reactor.core.publisher.Flux.from;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.security.SecurityFilter;

import org.reactivestreams.Publisher;

/**
 * Filters the flow using the specified {@link SecurityFilter}. If unauthorised the flow is stopped and therefore the message is
 * not send or dispatched by the transport. When unauthorised the request message is returned as the response.
 */
public class SecurityFilterMessageProcessor extends AbstractComponent
    implements Processor, Initialisable, ZernovaContextAware {

  private ZernovaContext muleContext;

  private SecurityFilter filter;

  public SecurityFilterMessageProcessor(SecurityFilter filter) {
    this.filter = filter;
  }

  @Override
  public void initialise() throws InitialisationException {
    try {
      muleContext.getInjector().inject(filter);
      initialiseIfNeeded(filter, muleContext);
    } catch (ZernovaException e) {
      throw new InitialisationException(e, this);
    }
  }

  public SecurityFilter getFilter() {
    return filter;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    return processToApply(event, this);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return from(publisher).map(event -> {
      try {
        return CoreEvent.builder(event).securityContext(filter.doFilter(event)).build();
      } catch (Exception e) {
        throw propagate(e);
      }
    });
  }

  public void setFilter(SecurityFilter filter) {
    this.filter = filter;
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }
}
