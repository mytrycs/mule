/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.notification;

import com.zernova.functional.api.component.FunctionalTestProcessor;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * By implementing this listener interface and registering the object with the
 * {@link NotificationListenerRegistry#registerListener(NotificationListener)} You can receive {@link FunctionalTestNotification}s
 * from the {@link FunctionalTestProcessor}.
 *
 * This Notification contains the current {@link CoreEvent}, {@link FlowConstruct} and reply message. The resource identifier for
 * this event is the service name that received the message. This means you can register to listen to Notifications from a
 * selected {@link FunctionalTestProcessor}. i.e. <code>
 * muleContext.registerListener(this, "*JmsTestCompoennt");
 * </code>
 *
 * This registration would only receive {@link FunctionalTestNotification} objects from components called 'MyJmsTestComponent' and
 * 'YourJmsTestComponent' but not 'HerFileTestComponent'.
 *
 * To receive all notifications simply do - <code>
 * muleContext.registerListener(this");
 * </code>
 *
 * @see FunctionalTestProcessor
 * @see FunctionalTestNotificationListener
 * @see ZernovaContext
 */
public interface FunctionalTestNotificationListener extends CustomNotificationListener<FunctionalTestNotification> {

  @Override
  default boolean isBlocking() {
    return false;
  }
}
