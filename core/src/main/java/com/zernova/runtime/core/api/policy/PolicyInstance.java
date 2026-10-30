/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.policy;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategySupplier;

import java.util.Optional;

/**
 * Instance of a policy which has {@link com.zernova.runtime.core.api.processor.Processor}s to be applied to a source and another
 * chain to be applied to an operation.
 *
 * Both of them are optionals since a policy may apply only to a source or only to an operation.
 *
 * @since 4.0
 */
@NoImplement
public interface PolicyInstance extends Initialisable, Startable, ProcessingStrategySupplier {

  /**
   * @return chain of processors to intercept the source execution
   */
  Optional<PolicyChain> getSourcePolicyChain();

  /**
   * @return chain of processors to intercept an operation execution.
   */
  Optional<PolicyChain> getOperationPolicyChain();

}
