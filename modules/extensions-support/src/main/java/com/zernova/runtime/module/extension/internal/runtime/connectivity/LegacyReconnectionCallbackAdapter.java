/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.extension.api.runtime.connectivity.ReconnectionCallback;

/**
 * Adapts a {@link com.zernova.sdk.api.runtime.connectivity.ReconnectionCallback} into a legacy {@link ReconnectionCallback}
 *
 * @since 4.5.0
 */
public class LegacyReconnectionCallbackAdapter implements ReconnectionCallback {

  private final com.zernova.sdk.api.runtime.connectivity.ReconnectionCallback delegate;

  public LegacyReconnectionCallbackAdapter(com.zernova.sdk.api.runtime.connectivity.ReconnectionCallback reconnectionCallback) {
    this.delegate = reconnectionCallback;
  }

  @Override
  public void success() {
    delegate.success();
  }

  @Override
  public void failed(ConnectionException exception) {
    delegate.failed(exception);
  }
}
