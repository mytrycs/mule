/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.http.api.server.async.ResponseStatusCallback;

public class ResponseStatusCallbackWrapper implements ResponseStatusCallback {

  private final com.zernova.sdk.api.http.server.async.ResponseStatusCallback sdkCallback;

  public ResponseStatusCallbackWrapper(com.zernova.sdk.api.http.server.async.ResponseStatusCallback sdkCallback) {
    this.sdkCallback = sdkCallback;
  }

  @Override
  public void responseSendFailure(Throwable throwable) {
    sdkCallback.responseSendFailure(throwable);
  }

  @Override
  public void responseSendSuccessfully() {
    sdkCallback.responseSendSuccessfully();
  }

  @Override
  public void onErrorSendingResponse(Throwable throwable) {
    sdkCallback.onErrorSendingResponse(throwable);
  }
}
