/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.application;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.core.api.policy.PolicyParametrization;
import com.zernova.runtime.deployment.model.api.policy.PolicyRegistrationException;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;

/**
 * Manages the policies that must be applied to a given application
 */
@NoImplement
public interface ApplicationPolicyManager {

  /**
   * Add a new policy
   *
   * @param policyTemplateDescriptor describes how to create the policy template. Non null
   * @param parametrization          parametrization used to instantiate the policy. Non null
   * @throws PolicyRegistrationException if an error occurs registering the policy
   */
  void addPolicy(PolicyTemplateDescriptor policyTemplateDescriptor, PolicyParametrization parametrization)
      throws PolicyRegistrationException;

  /**
   * Removes a policy from the application
   *
   * @param parametrizedPolicyId identifies the parametrized policy. Non empty.?
   * @return true is the policy was previously applied to the application, false otherwise
   */
  boolean removePolicy(String parametrizedPolicyId);
}
