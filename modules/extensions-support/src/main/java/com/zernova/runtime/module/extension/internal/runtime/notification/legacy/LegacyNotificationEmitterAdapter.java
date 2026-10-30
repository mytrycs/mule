/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.notification.legacy;

import java.util.function.Supplier;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.notification.NotificationActionDefinition;
import com.zernova.runtime.extension.api.notification.NotificationEmitter;

/**
 * Adapts a {@link com.zernova.sdk.api.notification.NotificationEmitter} into a legacy {@link NotificationEmitter}
 */
public class LegacyNotificationEmitterAdapter implements NotificationEmitter {

  private final com.zernova.sdk.api.notification.NotificationEmitter delegate;

  public LegacyNotificationEmitterAdapter(com.zernova.sdk.api.notification.NotificationEmitter delegate) {
    this.delegate = delegate;
  }

  @Override
  public void fire(NotificationActionDefinition action, TypedValue<?> data) {
    delegate.fire(action, data);
  }

  @Override
  public void fireLazy(NotificationActionDefinition action, Supplier<?> dataValue, DataType dataType) {
    delegate.fireLazy(action, dataValue, dataType);
  }
}
