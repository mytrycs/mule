/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.source.SourceCallback} into a {@link SourceCallback}
 *
 * @param <T> the generic type of the output value
 * @param <A> the generic type of the message attributes
 * @since 4.4.0
 */
public class SdkSourceCallbackAdapter<T, A> implements SourceCallback<T, A> {

  private final com.zernova.runtime.extension.api.runtime.source.SourceCallback<T, A> delegate;

  public SdkSourceCallbackAdapter(com.zernova.runtime.extension.api.runtime.source.SourceCallback<T, A> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void handle(Result<T, A> result) {
    delegate.handle(LegacyResultAdapter.from(result));
  }

  @Override
  public void handle(Result<T, A> result, SourceCallbackContext context) {
    delegate.handle(LegacyResultAdapter.from(result), new LegacySourceCallbackContextAdapter(context));
  }

  @Override
  public void onConnectionException(ConnectionException e) {
    delegate.onConnectionException(e);
  }

  @Override
  public SourceCallbackContext createContext() {
    return new SdkSourceCallBackContextAdapter(delegate.createContext());
  }
}
