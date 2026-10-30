/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation.adapter;

import com.zernova.runtime.api.message.Message;
import com.zernova.sdk.api.runtime.operation.FlowListener;

import java.util.function.Consumer;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.operation.FlowListener} into a {@link FlowListener}
 *
 * @since 4.5.0
 */
public class SdkFlowListenerAdapter implements FlowListener {

  private final com.zernova.runtime.extension.api.runtime.operation.FlowListener delegate;

  public SdkFlowListenerAdapter(com.zernova.runtime.extension.api.runtime.operation.FlowListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onSuccess(Consumer<Message> handler) {
    delegate.onSuccess(handler);
  }

  @Override
  public void onError(Consumer<Exception> handler) {
    delegate.onError(handler);
  }

  @Override
  public void onComplete(Runnable handler) {
    delegate.onComplete(handler);
  }
}
