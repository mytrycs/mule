/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.sdk.api.http.server.EndpointLifecycleHandler;

public class EndpointAvailabilityHandlerWrapper implements EndpointLifecycleHandler {

  private final com.zernova.runtime.http.api.server.RequestHandlerManager delegate;

  public EndpointAvailabilityHandlerWrapper(com.zernova.runtime.http.api.server.RequestHandlerManager delegate) {
    this.delegate = delegate;
  }

  @Override
  public void unavailable() {
    delegate.stop();
  }

  @Override
  public void available() {
    delegate.start();
  }

  @Override
  public void remove() {
    delegate.dispose();
  }
}
