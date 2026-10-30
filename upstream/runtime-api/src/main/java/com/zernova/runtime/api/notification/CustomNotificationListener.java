/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.notification;

/**
 * <code>CustomNotificationListener</code> is an observer interface that can be used to listen for Custom notifications using
 * <code>ZernovaContext.fireCustomEvent(..)</code>. Custom notifications can be used by components and other objects such as routers,
 * transformers, agents, etc to communicate a change of state to each other.
 */
public interface CustomNotificationListener<T extends CustomNotification>
    extends NotificationListener<CustomNotification> {
  // no methods
}
