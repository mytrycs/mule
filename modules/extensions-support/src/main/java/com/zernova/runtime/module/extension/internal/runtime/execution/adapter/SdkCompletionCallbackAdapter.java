/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution.adapter;

import static com.zernova.runtime.module.extension.internal.runtime.source.legacy.LegacyResultAdapter.from;

import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.process.CompletionCallback;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.process.CompletionCallback} into a {@link CompletionCallback}
 *
 * @param <T> The generic type of the operation's output value
 * @param <A> The generic type of the operation's output attributes
 *
 * @since 4.5.0
 */
public class SdkCompletionCallbackAdapter<T, A> implements CompletionCallback<T, A> {

  private final com.zernova.runtime.extension.api.runtime.process.CompletionCallback delegate;

  public SdkCompletionCallbackAdapter(com.zernova.runtime.extension.api.runtime.process.CompletionCallback delegate) {
    this.delegate = delegate;
  }

  @Override
  public void success(Result<T, A> result) {
    delegate.success(from(result));
  }

  @Override
  public void error(Throwable e) {
    delegate.error(e);
  }
}
