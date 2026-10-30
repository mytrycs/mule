/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.core.api.policy.OperationPolicyParametersTransformer;
import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.core.api.policy.SourcePolicyParametersTransformer;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class CompositePolicyFactory {

  public SourcePolicy createSourcePolicy(List<Policy> innerKey, ReactiveProcessor flowExecutionProcessor,
                                         Optional<SourcePolicyParametersTransformer> lookupSourceParametersTransformer,
                                         SourcePolicyProcessorFactory sourcePolicyProcessorFactory,
                                         Function<MessagingException, MessagingException> resolver) {
    return new CompositeSourcePolicy(innerKey, flowExecutionProcessor,
                                     lookupSourceParametersTransformer,
                                     sourcePolicyProcessorFactory, resolver);
  }

  public OperationPolicy createOperationPolicy(Component operation, List<Policy> innerKey,
                                               Optional<OperationPolicyParametersTransformer> paramsTransformer,
                                               OperationPolicyProcessorFactory operationPolicyProcessorFactory,
                                               long shutdownTimeout, Scheduler scheduler,
                                               FeatureFlaggingService featureFlaggingService) {
    return new CompositeOperationPolicy(operation, innerKey, paramsTransformer,
                                        operationPolicyProcessorFactory,
                                        shutdownTimeout, scheduler, featureFlaggingService);
  }

}
