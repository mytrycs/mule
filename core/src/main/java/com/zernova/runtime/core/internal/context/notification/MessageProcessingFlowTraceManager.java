/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context.notification;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.ZernovaExceptionInfo;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.notification.EnrichedNotificationInfo;
import com.zernova.runtime.api.notification.MessageProcessorNotification;
import com.zernova.runtime.api.notification.PipelineMessageNotification;
import com.zernova.runtime.core.api.context.notification.FlowCallStack;
import com.zernova.runtime.core.api.context.notification.FlowTraceManager;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.execution.LocationExecutionContextProvider;
import com.zernova.runtime.core.privileged.event.DefaultFlowCallStack;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

/**
 * Manager for handling message processing troubleshooting data.
 */
public class MessageProcessingFlowTraceManager extends LocationExecutionContextProvider
    implements FlowTraceManager, Initialisable, Disposable {

  public static final String FLOW_STACK_INFO_KEY = ZernovaExceptionInfo.FLOW_STACK_INFO_KEY;

  private final FlowNotificationTextDebugger pipelineProcessorDebugger;
  private final MessageProcessorTextDebugger messageProcessorTextDebugger;

  private ServerNotificationManager notificationManager;

  private volatile boolean listenersAdded = false;



  public MessageProcessingFlowTraceManager() {
    messageProcessorTextDebugger = new MessageProcessorTextDebugger(this);
    pipelineProcessorDebugger = new FlowNotificationTextDebugger(this);
  }

  @Override
  public void initialise() throws InitialisationException {
    handleNotificationListeners();
  }

  @Override
  public void dispose() {
    removeNotificationListeners();
  }

  protected synchronized void handleNotificationListeners() {
    if (!notificationManager.isDisposed()) {
      if (!listenersAdded) {
        notificationManager.addListener(messageProcessorTextDebugger);
        notificationManager.addListener(pipelineProcessorDebugger);
        listenersAdded = true;
      }
    }
  }

  protected synchronized void removeNotificationListeners() {
    if (listenersAdded && !notificationManager.isDisposed()) {
      notificationManager.removeListener(messageProcessorTextDebugger);
      notificationManager.removeListener(pipelineProcessorDebugger);
      listenersAdded = false;
    }
  }

  /**
   * Callback method for when a message processor is about to be invoked.
   *
   * @see DefaultFlowCallStack#pushCurrentProcessorPath(String)
   *
   * @param notification the notification that contains the event and the processor that is about to be invoked.
   */
  public void onMessageProcessorNotificationPreInvoke(MessageProcessorNotification notification) {
    FlowCallStack flowCallStack = ((CoreEvent) notification.getEvent()).getFlowCallStack();
    if (flowCallStack != null) {
      final var representation = notification.getComponent().getRepresentation();
      ((DefaultFlowCallStack) flowCallStack).pushCurrentProcessorPath(representation,
                                                                      notification.getComponent().getLocation(),
                                                                      notification.getComponent().getAnnotations());
    }
  }

  /**
   * Callback method for when a flow or sub-flow called from a {@code flow-ref} component has been completed.
   *
   * @param notification the notification that contains the event and the processor that is about to be invoked.
   */
  public void onPipelineNotificationComplete(PipelineMessageNotification notification) {
    onFlowComplete(notification.getInfo());
  }

  /**
   * Callback method for when a flow or sub-flow is about to be called from a {@code flow-ref}.
   *
   * @param notification the notification that contains the event and the processor that is about to be invoked.
   */
  public void onPipelineNotificationStart(PipelineMessageNotification notification) {
    onFlowStart(notification.getInfo(), notification.getResourceIdentifier());
  }

  @Override
  public void onFlowStart(EnrichedNotificationInfo notificationInfo, String flowName) {
    // Nothing to do
  }

  @Override
  public void onFlowComplete(EnrichedNotificationInfo notificationInfo) {
    // Nothing to do
  }

  @Override
  public Map<String, Object> getContextInfo(EnrichedNotificationInfo notificationInfo, Component lastProcessed) {
    final Map<String, Object> info = new HashMap<>();
    info.putIfAbsent(FLOW_STACK_INFO_KEY, ((CoreEvent) notificationInfo.getEvent()).getFlowCallStack().clone());
    return info;
  }

  @Override
  public void putContextInfo(ZernovaExceptionInfo info, EnrichedNotificationInfo notificationInfo, Component lastProcessed) {
    if (info.getFlowStack() == null) {
      info.setFlowStack(((CoreEvent) notificationInfo.getEvent()).getFlowCallStack().clone());
    }
  }

  @Inject
  public void setNotificationManager(ServerNotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }
}
