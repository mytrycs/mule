/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context.notification;

import static com.zernova.runtime.api.notification.MessageProcessorNotification.MESSAGE_PROCESSOR_PRE_INVOKE;

import com.zernova.runtime.api.notification.MessageProcessorNotification;
import com.zernova.runtime.api.notification.MessageProcessorNotificationListener;

/**
 * Listener for MessageProcessorNotification that delegates notifications to NotificationTextDebugger
 */
public class MessageProcessorTextDebugger implements MessageProcessorNotificationListener<MessageProcessorNotification> {

  private final MessageProcessingFlowTraceManager messageProcessingFlowTraceManager;

  @Override
  public boolean isBlocking() {
    return false;
  }

  public MessageProcessorTextDebugger(MessageProcessingFlowTraceManager messageProcessingFlowTraceManager) {
    this.messageProcessingFlowTraceManager = messageProcessingFlowTraceManager;
  }

  @Override
  public void onNotification(MessageProcessorNotification notification) {
    if (notification.getAction().getActionId() == MESSAGE_PROCESSOR_PRE_INVOKE) {
      messageProcessingFlowTraceManager.onMessageProcessorNotificationPreInvoke(notification);
    }
  }

}
