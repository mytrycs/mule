/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.notification;

import static java.lang.String.format;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.notification.HasNotifications;
import com.zernova.runtime.api.meta.model.notification.NotificationModel;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.sdk.api.notification.NotificationActionDefinition;
import com.zernova.sdk.api.notification.NotificationEmitter;

import java.util.function.Supplier;

/**
 * Default implementation of {@link NotificationEmitter}.
 *
 * @since 4.1
 */
public class DefaultNotificationEmitter implements NotificationEmitter {

  private final ServerNotificationManager notificationManager;
  private final CoreEvent event;
  private final Component component;
  private final HasNotifications componentModel;

  public DefaultNotificationEmitter(ServerNotificationManager notificationManager, CoreEvent event,
                                    Component component, ComponentModel componentModel) {
    this.notificationManager = notificationManager;
    this.event = event;
    this.component = component;
    this.componentModel = (HasNotifications) componentModel;
  }

  @Override
  public void fire(NotificationActionDefinition action, TypedValue<?> data) {
    validateAction((Enum) action);
    notificationManager.fireNotification(new DefaultExtensionNotification(event, component, action, data));
  }

  @Override
  public void fireLazy(NotificationActionDefinition action, Supplier<?> data, DataType actualDataType) {
    validateAction((Enum) action);
    notificationManager.fireNotification(new DefaultExtensionNotification(event, component, action, data, actualDataType));
  }

  private void validateAction(Enum action) {
    String actionName = action.name();

    for (NotificationModel nm : componentModel.getNotificationModels()) {
      if (actionName.equals(nm.getIdentifier())) {
        return;
      }
    }

    throw new IllegalArgumentException(format("Cannot fire '%s' notification since it's not declared by the component.",
                                              actionName));
  }

}
