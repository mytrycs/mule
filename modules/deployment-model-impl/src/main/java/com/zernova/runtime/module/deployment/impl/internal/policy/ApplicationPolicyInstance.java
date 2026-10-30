/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplate;
import com.zernova.runtime.policy.api.PolicyPointcut;

import java.util.Optional;

/**
 * Defines a policy provider for a given parametrized policy
 */
public interface ApplicationPolicyInstance extends Initialisable, Disposable {

  /**
   * @return the policy's pointcut used to determine whether to apply or ignore the policy when a request arrives. No null.
   */
  PolicyPointcut getPointcut();

  /**
   * @return order that must be used to apply the policy
   */
  int getOrder();

  /**
   * @return policy template from which the instance will be created
   */
  PolicyTemplate getPolicyTemplate();

  /**
   * @return policy to intercept the source execution
   */
  Optional<Policy> getSourcePolicy();

  /**
   * @return policy to intercept the operation execution
   */
  Optional<Policy> getOperationPolicy();
}
