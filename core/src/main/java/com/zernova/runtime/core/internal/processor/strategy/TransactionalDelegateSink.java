/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.transaction.TransactionCoordination.isTransactionActive;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.core.api.construct.BackPressureReason;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate {@link Sink} that uses one of two {@link Sink}'s depending on if a transaction is in context or not.
 */
final class TransactionalDelegateSink implements Sink, Disposable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalDelegateSink.class);

  private final Sink transactionalSink;
  private final Sink sink;

  public TransactionalDelegateSink(Sink transactionalSink, Sink sink) {
    this.transactionalSink = transactionalSink;
    this.sink = sink;
  }

  @Override
  public void accept(CoreEvent event) {
    if (isTransactionActive()) {
      transactionalSink.accept(event);
    } else {
      sink.accept(event);
    }
  }

  @Override
  public BackPressureReason emit(CoreEvent event) {
    if (isTransactionActive()) {
      return transactionalSink.emit(event);
    } else {
      return sink.emit(event);
    }
  }

  @Override
  public void dispose() {
    disposeIfNeeded(transactionalSink, LOGGER);
    disposeIfNeeded(sink, LOGGER);
  }
}
