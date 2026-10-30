/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.processor;

import static com.zernova.runtime.core.api.retry.ReconnectionConfig.defaultReconnectionConfig;

import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.api.retry.policy.NoRetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.dsl.api.component.ComponentFactory;

/**
 * {@link ComponentFactory} which yields instances of {@link ReconnectionConfig}
 *
 * @since 4.0
 */
public class ReconnectionConfigObjectFactory extends AbstractComponentFactory<ReconnectionConfig> {

  private boolean failsDeployment = false;
  private RetryPolicyTemplate retryPolicyTemplate = new NoRetryPolicyTemplate();

  @Override
  public ReconnectionConfig doGetObject() throws Exception {
    return defaultReconnectionConfig(failsDeployment, retryPolicyTemplate);
  }

  public void setFailsDeployment(boolean failsDeployment) {
    this.failsDeployment = failsDeployment;
  }

  public void setRetryPolicyTemplate(RetryPolicyTemplate retryPolicyTemplate) {
    this.retryPolicyTemplate = retryPolicyTemplate;
  }
}
