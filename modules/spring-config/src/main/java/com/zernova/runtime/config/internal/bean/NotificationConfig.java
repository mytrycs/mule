/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.bean;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.notification.AsyncMessageNotification;
import com.zernova.runtime.api.notification.AsyncMessageNotificationListener;
import com.zernova.runtime.api.notification.ClusterNodeNotificationListener;
import com.zernova.runtime.api.notification.ConnectionNotification;
import com.zernova.runtime.api.notification.ConnectionNotificationListener;
import com.zernova.runtime.api.notification.ConnectorMessageNotification;
import com.zernova.runtime.api.notification.ConnectorMessageNotificationListener;
import com.zernova.runtime.api.notification.CustomNotification;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.ErrorHandlerNotification;
import com.zernova.runtime.api.notification.ErrorHandlerNotificationListener;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.ExtensionNotification;
import com.zernova.runtime.api.notification.ExtensionNotificationListener;
import com.zernova.runtime.api.notification.ManagementNotification;
import com.zernova.runtime.api.notification.ManagementNotificationListener;
import com.zernova.runtime.api.notification.MessageProcessorNotification;
import com.zernova.runtime.api.notification.MessageProcessorNotificationListener;
import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.PipelineMessageNotification;
import com.zernova.runtime.api.notification.PipelineMessageNotificationListener;
import com.zernova.runtime.api.notification.RoutingNotification;
import com.zernova.runtime.api.notification.RoutingNotificationListener;
import com.zernova.runtime.api.notification.SecurityNotification;
import com.zernova.runtime.api.notification.SecurityNotificationListener;
import com.zernova.runtime.api.notification.TransactionNotification;
import com.zernova.runtime.api.notification.TransactionNotificationListener;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotification;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Notification helper object for creating notification manager configuration.
 * <p>
 * For now we create specific types {@code Notification.EnabledNotification} and {@code Notification.DisabledNotification} for
 * elements that have children of the same type but that must be injected in different collections of the parent object. Ideally
 * this should not be needed or we don't have to support such use case anymore but for previous elements coming from previous mule
 * version we have to keep the XML syntax as it is.
 *
 * @since 4.0
 */
public abstract class NotificationConfig<N extends Notification, L extends NotificationListener<N>>
    extends AbstractComponent {

  public static final Map<String, Class<? extends Notification>> EVENT_MAP;
  public static final Map<String, Class<? extends NotificationListener>> INTERFACE_MAP;

  static {
    EVENT_MAP = ImmutableMap.<String, Class<? extends Notification>>builder()
        .put("CONTEXT", ZernovaContextNotification.class)
        .put("SECURITY", SecurityNotification.class)
        .put("CONNECTOR-MESSAGE", ConnectorMessageNotification.class)
        .put("MANAGEMENT", ManagementNotification.class)
        .put("MESSAGE-PROCESSOR", MessageProcessorNotification.class)
        .put("EXCEPTION-STRATEGY", ErrorHandlerNotification.class)
        .put("CONNECTION", ConnectionNotification.class)
        .put("CUSTOM", CustomNotification.class)
        .put("EXCEPTION", ExceptionNotification.class)
        .put("TRANSACTION", TransactionNotification.class)
        .put("ROUTING", RoutingNotification.class)
        .put("PIPELINE-MESSAGE", PipelineMessageNotification.class)
        .put("ASYNC-MESSAGE", AsyncMessageNotification.class)
        .put("EXTENSION", ExtensionNotification.class)
        .build();

    INTERFACE_MAP = ImmutableMap.<String, Class<? extends NotificationListener>>builder()
        .put("CONTEXT", ZernovaContextNotificationListener.class)
        .put("SECURITY", SecurityNotificationListener.class)
        .put("MANAGEMENT", ManagementNotificationListener.class)
        .put("MESSAGE-PROCESSOR", MessageProcessorNotificationListener.class)
        .put("EXCEPTION-STRATEGY", ErrorHandlerNotificationListener.class)
        .put("CONNECTION", ConnectionNotificationListener.class)
        .put("CUSTOM", CustomNotificationListener.class)
        .put("CONNECTOR-MESSAGE", ConnectorMessageNotificationListener.class)
        .put("EXCEPTION", ExceptionNotificationListener.class)
        .put("TRANSACTION", TransactionNotificationListener.class)
        .put("ROUTING", RoutingNotificationListener.class)
        .put("CLUSTER-NODE", ClusterNodeNotificationListener.class)
        .put("PIPELINE-MESSAGE", PipelineMessageNotificationListener.class)
        .put("ASYNC-MESSAGE", AsyncMessageNotificationListener.class)
        .put("EXTENSION", ExtensionNotificationListener.class)
        .build();
  }

  public NotificationConfig(Class<L> interfaceClass, Class<N> eventClass) {
    this.interfaceClass = interfaceClass;
    this.eventClass = eventClass;
  }

  public NotificationConfig() {
    // used by the parser builder.
  }

  private String eventName;
  private Class<N> eventClass;
  private String interfaceName;
  private Class<L> interfaceClass;
  private boolean eventExplicitlyConfigured;
  private boolean interfaceExplicitlyConfigured;

  public void setInterfaceName(String interfaceName) {
    interfaceExplicitlyConfigured = true;
    this.interfaceName = interfaceName;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setEventClass(Class<N> eventClass) {
    eventExplicitlyConfigured = true;
    this.eventClass = eventClass;
  }

  public Class<N> getEventClass() {
    return eventClass;
  }

  public void setEventName(String eventName) {
    eventExplicitlyConfigured = true;
    this.eventName = eventName;
  }

  public String getEventName() {
    return eventName;
  }

  public void setInterfaceClass(Class<L> interfaceClass) {
    interfaceExplicitlyConfigured = true;
    this.interfaceClass = interfaceClass;
  }

  public Class<L> getInterfaceClass() {
    return interfaceClass;
  }

  public boolean isEventExplicitlyConfigured() {
    return eventExplicitlyConfigured;
  }

  public boolean isInterfaceExplicitlyConfigured() {
    return interfaceExplicitlyConfigured;
  }

  public static class EnabledNotificationConfig<N extends Notification, L extends NotificationListener<N>>
      extends NotificationConfig<N, L> {

    public EnabledNotificationConfig(Class<L> interfaceClass, Class<N> eventClass) {
      super(interfaceClass, eventClass);
    }

    public EnabledNotificationConfig() {
      super();
    }
  }

  public static class DisabledNotificationConfig<N extends Notification, L extends NotificationListener<N>>
      extends NotificationConfig<N, L> {

    public DisabledNotificationConfig() {
      super();
    }
  }

}
