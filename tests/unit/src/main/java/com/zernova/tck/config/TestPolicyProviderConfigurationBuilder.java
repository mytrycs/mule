/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.config;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_POLICY_PROVIDER;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.internal.policy.NullPolicyProvider;

public class TestPolicyProviderConfigurationBuilder extends AbstractConfigurationBuilder {

  @Override
  protected void doConfigure(ZernovaContext muleContext) {
    muleContext.getCustomizationService().registerCustomServiceImpl(OBJECT_POLICY_PROVIDER, new NullPolicyProvider());
  }

}
