/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import static com.zernova.runtime.module.extension.internal.runtime.source.legacy.SdkBackPressureActionUtils.from;

import com.zernova.runtime.api.event.Event;
import com.zernova.sdk.api.runtime.source.BackPressureAction;
import com.zernova.sdk.api.runtime.source.BackPressureContext;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.source.BackPressureContext} into a {@link BackPressureContext}
 *
 * @since 4.5.0
 */
public class SdkBackPressureContextAdapter implements BackPressureContext {

  private final com.zernova.runtime.extension.api.runtime.source.BackPressureContext delegate;

  public SdkBackPressureContextAdapter(com.zernova.runtime.extension.api.runtime.source.BackPressureContext delegate) {
    this.delegate = delegate;
  }

  @Override
  public BackPressureAction getAction() {
    return from(delegate.getAction());
  }

  @Override
  public Event getEvent() {
    return delegate.getEvent();
  }

  @Override
  public SourceCallbackContext getSourceCallbackContext() {
    return new SdkSourceCallBackContextAdapter(delegate.getSourceCallbackContext());
  }
}
