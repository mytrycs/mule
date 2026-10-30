/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing.forkjoin;

import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processWithChildContextDontComplete;

import static java.util.Optional.empty;

import com.zernova.runtime.api.config.ZernovaRuntimeFeature;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;

import org.reactivestreams.Publisher;

/**
 * Implementation of {@link RoutePairPublisherAssemblyHelper} which does not complete the child contexts on timeout.
 * <p>
 * Exists as a fallback for the kill switch COMPLETE_CHILDREN_ON_TIMEOUT.
 *
 * @implNote This implementation can be removed once the {@link ZernovaRuntimeFeature#FORK_JOIN_COMPLETE_CHILDREN_ON_TIMEOUT} feature
 *           flag is removed
 */
class LegacyRoutePairPublisherAssemblyHelper implements RoutePairPublisherAssemblyHelper {

  private final Publisher<CoreEvent> publisherWithChildContext;

  LegacyRoutePairPublisherAssemblyHelper(CoreEvent routeEvent, ReactiveProcessor chain) {
    this.publisherWithChildContext = processWithChildContextDontComplete(routeEvent, chain, empty());
  }

  @Override
  public Publisher<CoreEvent> getPublisherOnChildContext() {
    return publisherWithChildContext;
  }

  @Override
  public Publisher<CoreEvent> decorateTimeoutPublisher(Publisher<CoreEvent> timeoutPublisher) {
    // Adds nothing to the timeout publisher
    return timeoutPublisher;
  }
}
