/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.util.Pair;

import java.util.Map;

/**
 * Plugins may implement this interface and register that implementation in the bootstrap to declare the notification types it
 * supports.
 *
 * @since 4.0
 */
@NoImplement
public interface NotificationsProvider {

  /**
   * The key of the returned map is the string representation of the notification. It must be in the format
   * {@code [artifactid]:[NOTIFICATION-ID]}
   *
   * @return the mapping of the notification ID to the concrete types of {@link Notification} and {@link NotificationListener} it
   *         represents.
   */
  Map<String, Pair<Class<? extends Notification>, Class<? extends NotificationListener>>> getEventListenerMapping();
}
