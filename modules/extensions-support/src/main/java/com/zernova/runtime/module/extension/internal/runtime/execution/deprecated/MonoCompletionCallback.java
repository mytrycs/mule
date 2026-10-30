/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution.deprecated;

import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;

import reactor.core.publisher.MonoSink;

/**
 * Implementation of {@link CompletionCallback} which works using project Reactor.
 *
 * @since 4.0
 * @deprecated since 4.3
 */
@Deprecated
final class MonoCompletionCallback implements CompletionCallback<Object, Object> {

  private final MonoSink<Object> sink;

  MonoCompletionCallback(MonoSink<Object> sink) {
    this.sink = sink;
  }

  @Override
  public void success(Result<Object, Object> result) {
    sink.success(result);
  }

  @Override
  public void error(Throwable e) {
    sink.error(e);
  }
}
