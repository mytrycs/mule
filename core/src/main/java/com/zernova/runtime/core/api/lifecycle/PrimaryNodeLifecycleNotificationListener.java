/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.lifecycle;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.notification.ClusterNodeNotification;
import com.zernova.runtime.api.notification.ClusterNodeNotificationListener;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class will start an Startable mule object that must only be started in the primary node.
 *
 */
public final class PrimaryNodeLifecycleNotificationListener implements ClusterNodeNotificationListener {

  protected transient Logger logger = LoggerFactory.getLogger(getClass());
  private Startable startMeOnPrimaryNodeNotification;
  private NotificationListenerRegistry notificationListenerRegistry;

  public PrimaryNodeLifecycleNotificationListener(Startable startMeOnPrimaryNodeNotification,
                                                  NotificationListenerRegistry notificationListenerRegistry) {
    this.startMeOnPrimaryNodeNotification = startMeOnPrimaryNodeNotification;
    this.notificationListenerRegistry = notificationListenerRegistry;
  }

  public void register() {
    if (notificationListenerRegistry != null) {
      notificationListenerRegistry.registerListener(this);
    }
  }

  @Override
  public void onNotification(ClusterNodeNotification notification) {
    try {
      if (startMeOnPrimaryNodeNotification instanceof LifecycleState) {
        if (((LifecycleState) startMeOnPrimaryNodeNotification).isStarted()) {
          startMeOnPrimaryNodeNotification.start();
        } else {
          logStartableNotStartedMessage();
        }
      } else if (startMeOnPrimaryNodeNotification instanceof LifecycleStateEnabled) {
        if (((LifecycleStateEnabled) startMeOnPrimaryNodeNotification).getLifecycleState().isStarted()) {
          startMeOnPrimaryNodeNotification.start();
        } else {
          logStartableNotStartedMessage();
        }
      } else {
        startMeOnPrimaryNodeNotification.start();
      }
    } catch (ZernovaException e) {
      throw new RuntimeException("Error starting wrapped message source", e);
    }
  }

  private void logStartableNotStartedMessage() {
    if (logger.isDebugEnabled()) {
      logger.debug("Not starting Startable since it's not in started state");
    }
  }

  public void unregister() {
    notificationListenerRegistry.unregisterListener(this);
  }
}
