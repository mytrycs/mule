/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

/**
 * A {@link ArgumentResolver} that returns the {@link RetryPolicyTemplate} effective for the operation
 *
 * @since 4.2.2 - 4.3.0
 */
public class RetryPolicyTemplateArgumentResolver implements ArgumentResolver<RetryPolicyTemplate> {

  @Override
  public RetryPolicyTemplate resolve(ExecutionContext executionContext) {
    ExecutionContextAdapter<OperationModel> ctx = (ExecutionContextAdapter<OperationModel>) executionContext;
    return ctx.getRetryPolicyTemplate().get();
  }
}
