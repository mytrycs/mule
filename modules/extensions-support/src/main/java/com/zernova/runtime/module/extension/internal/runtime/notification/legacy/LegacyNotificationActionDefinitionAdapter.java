/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.notification.legacy;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.extension.api.notification.NotificationActionDefinition;

/**
 * Adapts a {@link com.zernova.sdk.api.notification.NotificationActionDefinition} into a legacy {@link NotificationActionDefinition}
 *
 * @since 4.4.0
 */
public class LegacyNotificationActionDefinitionAdapter<E extends Enum<E>> implements NotificationActionDefinition<E> {

  private final com.zernova.sdk.api.notification.NotificationActionDefinition delegate;

  public LegacyNotificationActionDefinitionAdapter(com.zernova.sdk.api.notification.NotificationActionDefinition<?> delegate) {
    this.delegate = delegate;
  }

  @Override
  public DataType getDataType() {
    return delegate.getDataType();
  }
}
