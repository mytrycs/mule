/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.runtime.api.notification.NotificationListener;

/**
 * <code>ZernovaContextNotificationListener</code> is an observer interface that objects can implement and then register themselves
 * with the Mule manager to be notified when a Manager event occurs.
 */
public interface ZernovaContextNotificationListener<T extends ZernovaContextNotification>
    extends NotificationListener<ZernovaContextNotification> {
  // no methods
}
