/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context.notification;

import static com.zernova.runtime.api.notification.PipelineMessageNotification.PROCESS_COMPLETE;
import static com.zernova.runtime.api.notification.PipelineMessageNotification.PROCESS_START;

import com.zernova.runtime.api.notification.PipelineMessageNotification;
import com.zernova.runtime.api.notification.PipelineMessageNotificationListener;

/**
 * Listener for PipelineMessageNotification that delegates notifications to NotificationTextDebugger
 */
public class FlowNotificationTextDebugger implements PipelineMessageNotificationListener<PipelineMessageNotification> {

  private final MessageProcessingFlowTraceManager messageProcessingFlowTraceManager;

  public FlowNotificationTextDebugger(MessageProcessingFlowTraceManager messageProcessingFlowTraceManager) {
    this.messageProcessingFlowTraceManager = messageProcessingFlowTraceManager;
  }

  @Override
  public boolean isBlocking() {
    return false;
  }

  @Override
  public void onNotification(PipelineMessageNotification notification) {
    if (notification.getAction().getActionId() == PROCESS_COMPLETE) {
      messageProcessingFlowTraceManager.onPipelineNotificationComplete(notification);
    } else if (notification.getAction().getActionId() == PROCESS_START) {
      messageProcessingFlowTraceManager.onPipelineNotificationStart(notification);
    }
  }


}
