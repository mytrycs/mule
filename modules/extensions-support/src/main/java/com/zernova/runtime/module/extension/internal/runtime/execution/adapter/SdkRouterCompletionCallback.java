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
import com.zernova.sdk.api.runtime.process.RouterCompletionCallback;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback} into a
 * {@link RouterCompletionCallback}
 *
 * @since 4.5.0
 */
public class SdkRouterCompletionCallback implements RouterCompletionCallback {

  private com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback delegate;

  public SdkRouterCompletionCallback(com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback delegate) {
    this.delegate = delegate;
  }

  @Override
  public void success(Result<Object, Object> result) {
    delegate.success(from(result));
  }

  @Override
  public void error(Throwable throwable) {
    delegate.error(throwable);
  }
}
