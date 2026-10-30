/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.operation.FlowListener;
import com.zernova.runtime.module.extension.api.runtime.privileged.EventedExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.operation.DefaultFlowListener;

/**
 * An {@link ArgumentResolver} which produces instances of {@link FlowListener}
 *
 * @since 4.0
 */
public class FlowListenerArgumentResolver implements ArgumentResolver<FlowListener> {

  @Override
  public FlowListener resolve(ExecutionContext executionContext) {
    return new DefaultFlowListener(executionContext.getExtensionModel(),
                                   (OperationModel) executionContext.getComponentModel(),
                                   ((EventedExecutionContext) executionContext).getEvent());

  }
}
