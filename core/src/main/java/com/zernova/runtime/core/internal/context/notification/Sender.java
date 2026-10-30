/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context.notification;

import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.core.api.context.notification.ListenerSubscriptionPair;
import com.zernova.runtime.core.api.context.notification.NotifierCallback;

/**
 * This does the work necessary to deliver events to a particular listener. It is generated for a particular {@link Configuration}
 * and stored in a {@link Policy}.
 */
public class Sender<N extends Notification> {

  private ListenerSubscriptionPair<N> pair;

  Sender(ListenerSubscriptionPair<N> pair) {
    this.pair = pair;
  }

  public void dispatch(N notification, NotifierCallback notifier) {
    if (pair.getSelector().test(notification)) {
      notifier.notify(pair.getListener(), notification);
    }
  }

  NotificationListener<N> getListener() {
    return pair.getListener();
  }
}

