/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.config;

import static com.zernova.runtime.core.api.context.notification.ZernovaContextNotification.CONTEXT_STARTED;

import com.zernova.runtime.api.notification.IntegerAction;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotification;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.context.notification.DefaultNotificationListenerRegistry;
import com.zernova.runtime.core.privileged.registry.RegistrationException;

public class TestNotificationListenerRegistryConfigurationBuilder extends AbstractConfigurationBuilder {

  private Latch contextStartedLatch;

  public TestNotificationListenerRegistryConfigurationBuilder(Latch contextStartedLatch) {
    this.contextStartedLatch = contextStartedLatch;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws RegistrationException {
    final DefaultNotificationListenerRegistry notificationListenerRegistry = new DefaultNotificationListenerRegistry();
    notificationListenerRegistry.setNotificationManager(muleContext.getNotificationManager());
    notificationListenerRegistry.registerListener(new ZernovaContextNotificationListener<>() {

      @Override
      public boolean isBlocking() {
        return false;
      }

      @Override
      public void onNotification(ZernovaContextNotification notification) {
        if (new IntegerAction(CONTEXT_STARTED).equals(notification.getAction())) {
          contextStartedLatch.countDown();
        }
      }
    });

    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(NotificationListenerRegistry.REGISTRY_KEY,
                                                                         notificationListenerRegistry);
  }
}
