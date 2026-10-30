/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.retry.async;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.retry.RetryCallback;
import com.zernova.runtime.core.api.retry.RetryContext;
import com.zernova.runtime.core.api.retry.RetryNotifier;
import com.zernova.runtime.core.api.retry.policy.RetryPolicy;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.internal.retry.async.FutureRetryContext;
import com.zernova.runtime.core.internal.retry.async.RetryWorker;

import java.util.Map;
import java.util.concurrent.Executor;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * This class is a wrapper for a {@link RetryPolicyTemplate} and will execute any retry work within a separate thread. An optional
 * {@link Latch} can be passed into this template, in which case execution will only occur once the latch is released.
 */
public final class AsynchronousRetryTemplate extends AbstractComponent
    implements RetryPolicyTemplate, Initialisable, Startable, Stoppable, Disposable {

  private final RetryPolicyTemplate delegate;
  private Latch startLatch;

  @Inject
  private ZernovaContext muleContext;

  private static final Logger LOGGER = getLogger(AsynchronousRetryTemplate.class);

  public AsynchronousRetryTemplate(RetryPolicyTemplate delegate) {
    this.delegate = delegate;
  }

  @Override
  public RetryContext execute(RetryCallback callback, Executor workManager) throws Exception {
    if (workManager == null) {
      throw new IllegalStateException("Cannot schedule a work till the workManager is initialized. Probably the connector hasn't been initialized yet");
    }

    RetryWorker worker = new RetryWorker(delegate, callback, workManager, startLatch);
    FutureRetryContext context = worker.getRetryContext();

    workManager.execute(worker);
    return context;
  }

  @Override
  public boolean isEnabled() {
    return delegate.isEnabled();
  }

  @Override
  public RetryPolicy createRetryInstance() {
    return delegate.createRetryInstance();
  }

  @Override
  public RetryNotifier getNotifier() {
    return delegate.getNotifier();
  }

  @Override
  public void setNotifier(RetryNotifier retryNotifier) {
    delegate.setNotifier(retryNotifier);
  }

  @Override
  public Map<Object, Object> getMetaInfo() {
    return delegate.getMetaInfo();
  }

  @Override
  public void setMetaInfo(Map<Object, Object> metaInfo) {
    delegate.setMetaInfo(metaInfo);
  }

  public RetryPolicyTemplate getDelegate() {
    return delegate;
  }

  public void setStartLatch(Latch latch) {
    this.startLatch = latch;
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
    disposeIfNeeded(delegate, LOGGER);
  }

  @Override
  public boolean isAsync() {
    return true;
  }
}
