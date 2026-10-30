/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.services;

import static java.util.Collections.emptyList;
import static com.zernova.runtime.http.policy.api.SourcePolicyAwareAttributes.noAttributes;

import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.core.api.policy.PolicyProvider;
import com.zernova.runtime.policy.api.PolicyAwareAttributes;
import com.zernova.runtime.policy.api.PolicyPointcutParameters;

import java.util.List;

/**
 * A policy provider that returns no policy.
 *
 * @since 4.0
 */
public class NullPolicyProvider implements PolicyProvider {

  @Override
  public List<Policy> findSourceParameterizedPolicies(PolicyPointcutParameters policyPointcutParameters) {
    return emptyList();
  }

  @Override
  public PolicyAwareAttributes sourcePolicyAwareAttributes() {
    return noAttributes();
  }

  @Override
  public boolean isSourcePoliciesAvailable() {
    return false;
  }

  @Override
  public boolean isOperationPoliciesAvailable() {
    return false;
  }

  @Override
  public List<Policy> findOperationParameterizedPolicies(PolicyPointcutParameters policyPointcutParameters) {
    return emptyList();
  }
}
