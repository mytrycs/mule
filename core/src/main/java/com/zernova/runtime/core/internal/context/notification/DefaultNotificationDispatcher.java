/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context.notification;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Implementation of {@link NotificationDispatcher} that just forwards events to the {@link ZernovaContext}.
 *
 * @since 4.0
 */
public class DefaultNotificationDispatcher implements NotificationDispatcher {

  private static final Logger LOGGER = getLogger(DefaultNotificationDispatcher.class);

  @Inject
  private ZernovaContext context;

  public ZernovaContext getContext() {
    return context;
  }

  @Override
  public void dispatch(Notification notification) {
    ServerNotificationManager notificationManager = context.getNotificationManager();
    if (notificationManager != null) {
      notificationManager.fireNotification(notification);
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("ZernovaEvent Manager is not enabled, ignoring notification: " + notification);
    }
  }

}
