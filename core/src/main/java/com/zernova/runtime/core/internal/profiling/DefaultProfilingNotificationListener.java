/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingEventContext;
import com.zernova.runtime.core.internal.profiling.notification.ProfilingNotificationListener;
import com.zernova.runtime.core.internal.profiling.notification.ProfilingNotification;

/**
 * A {@link NotificationListener} that listens for profiling notifications.
 *
 * @since 4.4
 */
public class DefaultProfilingNotificationListener<T extends ProfilingEventContext> implements ProfilingNotificationListener<T> {

  private final ProfilingDataConsumer<T> dataConsumer;

  @Override
  public boolean isBlocking() {
    return false;
  }

  @Override
  public void onNotification(ProfilingNotification<T> notification) {
    dataConsumer.onProfilingEvent(notification.getProfilingEventType(), (T) notification.getSource());
  }

  public DefaultProfilingNotificationListener(ProfilingDataConsumer<T> dataConsumer) {
    this.dataConsumer = dataConsumer;
  }
}
