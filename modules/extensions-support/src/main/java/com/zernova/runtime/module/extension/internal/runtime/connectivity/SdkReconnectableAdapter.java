/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.sdk.api.runtime.connectivity.Reconnectable;
import com.zernova.sdk.api.runtime.connectivity.ReconnectionCallback;

import java.util.Optional;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.connectivity.Reconnectable} into an sdk-api {@link Reconnectable}
 *
 * @since 4.5.0
 */
public class SdkReconnectableAdapter implements Reconnectable {

  private final com.zernova.runtime.extension.api.runtime.connectivity.Reconnectable delegate;

  public static Optional<Reconnectable> from(Object value) {
    if (value instanceof Reconnectable) {
      return of((Reconnectable) value);
    } else if (value instanceof com.zernova.runtime.extension.api.runtime.connectivity.Reconnectable) {
      return of(new SdkReconnectableAdapter((com.zernova.runtime.extension.api.runtime.connectivity.Reconnectable) value));
    } else {
      return empty();
    }
  }

  public SdkReconnectableAdapter(com.zernova.runtime.extension.api.runtime.connectivity.Reconnectable reconnectable) {
    this.delegate = reconnectable;
  }

  @Override
  public void reconnect(ConnectionException exception, ReconnectionCallback reconnectionCallback) {
    delegate.reconnect(exception, new LegacyReconnectionCallbackAdapter(reconnectionCallback));
  }
}
