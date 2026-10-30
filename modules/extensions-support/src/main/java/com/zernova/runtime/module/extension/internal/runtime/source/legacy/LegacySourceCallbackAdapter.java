/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.core.internal.util.message.SdkResultAdapter;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.sdk.api.runtime.source.SourceCallback;

/**
 * Adapts a {@link SourceCallback} into a legacy {@link com.zernova.runtime.extension.api.runtime.source.SourceCallback}
 *
 * @param <T> the generic type of the output value
 * @param <A> the generic type of the message attributes
 * @since 4.4.0
 */
public class LegacySourceCallbackAdapter<T, A>
    implements com.zernova.runtime.extension.api.runtime.source.SourceCallback<T, A> {

  private final SourceCallback<T, A> sourceCallback;

  public LegacySourceCallbackAdapter(SourceCallback<T, A> sourceCallback) {
    this.sourceCallback = sourceCallback;
  }

  @Override
  public void handle(Result<T, A> result) {
    sourceCallback.handle(SdkResultAdapter.from(result));
  }

  @Override
  public void handle(Result<T, A> result, SourceCallbackContext context) {
    sourceCallback.handle(SdkResultAdapter.from(result), new SdkSourceCallBackContextAdapter(context));
  }

  @Override
  public void onConnectionException(ConnectionException e) {
    sourceCallback.onConnectionException(e);
  }

  @Override
  public SourceCallbackContext createContext() {
    return new LegacySourceCallbackContextAdapter(sourceCallback.createContext());
  }
}
