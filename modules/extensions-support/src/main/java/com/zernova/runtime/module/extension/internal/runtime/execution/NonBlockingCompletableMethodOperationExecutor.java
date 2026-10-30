/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import static com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties.COMPLETION_CALLBACK_CONTEXT_PARAM;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

import java.lang.reflect.Method;

/**
 * Implementation of {@link NonBlockingCompletableMethodOperationExecutor} which works by using a
 * {@link GeneratedMethodComponentExecutor}
 *
 * @since 4.3.0
 */
public class NonBlockingCompletableMethodOperationExecutor<M extends ComponentModel>
    extends AbstractCompletableMethodOperationExecutor<M> {

  public NonBlockingCompletableMethodOperationExecutor(M operationModel, Method operationMethod, Object operationInstance) {
    super(operationModel, operationMethod, operationInstance);
  }

  @Override
  protected void doExecute(ExecutionContext<M> executionContext, ExecutorCallback callback) {
    final ExecutionContextAdapter<M> context = (ExecutionContextAdapter<M>) executionContext;
    context.setVariable(COMPLETION_CALLBACK_CONTEXT_PARAM,
                        new ExecutorCompletionCallbackAdapter(new PreservingThreadContextExecutorCallback(callback)));

    executor.execute(executionContext);
  }

}
