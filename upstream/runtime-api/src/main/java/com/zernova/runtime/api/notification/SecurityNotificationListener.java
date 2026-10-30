/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.notification;

/**
 * <code>ZernovaContextNotificationListener</code> is an observer interface that objects can implement to receive notifications about
 * secure access requests.
 */
public interface SecurityNotificationListener<T extends SecurityNotification>
    extends NotificationListener<SecurityNotification> {
  // no methods
}
