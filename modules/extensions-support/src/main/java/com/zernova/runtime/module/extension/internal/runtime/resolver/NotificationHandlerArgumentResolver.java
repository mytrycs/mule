/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.extension.api.notification.NotificationEmitter;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.notification.DefaultNotificationEmitter;
import com.zernova.runtime.module.extension.internal.runtime.notification.legacy.LegacyNotificationEmitterAdapter;

/**
 * {@link ArgumentResolver} implementation for {@link NotificationEmitter} parameters.
 *
 * @since 4.1
 */
public class NotificationHandlerArgumentResolver implements ArgumentResolver<NotificationEmitter> {

  private final ArgumentResolver<com.zernova.sdk.api.notification.NotificationEmitter> notificationEmitterArgumentResolver =
      new SdkNotificationHandlerArgumentResolver();

  @Override
  public NotificationEmitter resolve(ExecutionContext executionContext) {
    com.zernova.sdk.api.notification.NotificationEmitter notificationEmitter =
        notificationEmitterArgumentResolver.resolve(executionContext);
    return notificationEmitter == null ? null : new LegacyNotificationEmitterAdapter(notificationEmitter);
  }
}
