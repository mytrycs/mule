/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static com.zernova.runtime.core.internal.policy.DefaultPolicyManager.noPolicyOperation;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.policy.api.PolicyPointcutParameters;

/**
 * Implementation of {@link PolicyManager} that does not apply any policy.
 *
 * @since 4.3
 */
public class NoOpPolicyManager implements PolicyManager {

  @Override
  public SourcePolicy createSourcePolicyInstance(Component source, CoreEvent sourceEvent,
                                                 ReactiveProcessor flowExecutionProcessor,
                                                 MessageSourceResponseParametersProcessor messageSourceResponseParametersProcessor) {
    return new NoSourcePolicy(flowExecutionProcessor);
  }

  @Override
  public PolicyPointcutParameters addSourcePointcutParametersIntoEvent(Component source, TypedValue<?> attributes,
                                                                       InternalEvent event) {
    return new PolicyPointcutParameters(source);
  }

  @Override
  public OperationPolicy createOperationPolicy(Component operation, CoreEvent operationEvent,
                                               OperationParametersProcessor operationParameters) {
    return noPolicyOperation();
  }

}
