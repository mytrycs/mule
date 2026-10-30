/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;

public interface OperationPolicy {

  /**
   * Process the policy chain of processors. The provided {@code nextOperation} function has the behaviour to be executed by the
   * next-operation of the chain.
   *
   * @param operationEvent             the event with the data to execute the operation
   * @param operationExecutionFunction the function that executes the operation.
   * @param parametersProcessor        the {@link OperationParametersProcessor} to apply
   * @param componentLocation          the location of the component on which the policy has been applied on
   * @param callback                   the {@link ExecutorCallback} on which the result of processing the {@code event} through
   *                                   the policy chain will be notified on
   */
  void process(CoreEvent operationEvent,
               OperationExecutionFunction operationExecutionFunction,
               OperationParametersProcessor parametersProcessor,
               ComponentLocation componentLocation,
               ExecutorCallback callback);

}
