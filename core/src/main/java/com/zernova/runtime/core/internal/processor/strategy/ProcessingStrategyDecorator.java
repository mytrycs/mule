/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.BackPressureReason;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.api.processor.Sink;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;

import java.util.concurrent.RejectedExecutionException;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;

/**
 * Base class for {@link ProcessingStrategy} decorators.
 * <p>
 * This class implements {@link Lifecycle} so that all phases are propagated to the {@link #delegate} if it implements the
 * corresponding interfaces.
 *
 * @since 4.3.0
 */
public abstract class ProcessingStrategyDecorator implements ProcessingStrategy, Lifecycle {

  protected final ProcessingStrategy delegate;

  @Inject
  private ZernovaContext muleContext;

  public ProcessingStrategyDecorator(ProcessingStrategy delegate) {
    this.delegate = delegate;
  }

  @Override
  public Sink createSink(FlowConstruct flowConstruct, ReactiveProcessor pipeline) {
    return delegate.createSink(flowConstruct, pipeline);
  }

  @Override
  public void registerInternalSink(Publisher<CoreEvent> publisher, String sinkRepresentation) {
    delegate.registerInternalSink(publisher, sinkRepresentation);
  }

  @Override
  public Publisher<CoreEvent> configureInternalPublisher(Publisher<CoreEvent> publisher) {
    return delegate.configureInternalPublisher(publisher);
  }

  @Override
  public ReactiveProcessor onPipeline(ReactiveProcessor pipeline) {
    return delegate.onPipeline(pipeline);
  }

  @Override
  public ReactiveProcessor onProcessor(ReactiveProcessor processor) {
    return delegate.onProcessor(processor);
  }

  @Override
  public boolean isSynchronous() {
    return delegate.isSynchronous();
  }

  @Override
  public void checkBackpressureAccepting(CoreEvent event) throws RejectedExecutionException {
    delegate.checkBackpressureAccepting(event);
  }

  @Override
  public BackPressureReason checkBackpressureEmitting(CoreEvent event) {
    return delegate.checkBackpressureEmitting(event);
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(delegate, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(delegate);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(delegate);
  }

  @Override
  public void dispose() {
    if (delegate instanceof Disposable) {
      ((Disposable) delegate).dispose();
    }
  }
}
