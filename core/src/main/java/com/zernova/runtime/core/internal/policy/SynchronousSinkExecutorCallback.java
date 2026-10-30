/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;

import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SynchronousSink;

/**
 * An {@link ExecutorCallback} which propagates the result of components in the form of a {@link CoreEvent}.
 * <p>
 * It propagates the generated event through a {@link MonoSink} received in the constructor
 *
 * @since 4.3.0
 */
public class SynchronousSinkExecutorCallback implements ExecutorCallback {

  private final SynchronousSink<CoreEvent> sink;

  /**
   * Creates a new instance
   *
   * @param sink the {@link MonoSink} through which the produced {@link CoreEvent} will be propagated
   */
  public SynchronousSinkExecutorCallback(SynchronousSink<CoreEvent> sink) {
    this.sink = sink;
  }

  @Override
  public void complete(Object value) {
    try {
      sink.next((CoreEvent) value);
    } catch (Throwable t) {
      sink.error(t);
    }
  }

  @Override
  public void error(Throwable e) {
    sink.error(e);
  }
}
