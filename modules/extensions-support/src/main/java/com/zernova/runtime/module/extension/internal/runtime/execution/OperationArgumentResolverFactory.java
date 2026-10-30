/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;

import java.util.Map;
import java.util.function.Function;

/**
 * Implementations provide a {@link Function} to get the parameters that will be passed to an operation from an
 * {@link OperationModel}.
 *
 * @since 4.0
 */
public interface OperationArgumentResolverFactory<T extends ComponentModel> {

  /**
   * Builds the argument resolver based on an {@code executionContext} and the internal state of this object.
   */
  Function<ExecutionContext<T>, Map<String, Object>> createArgumentResolver(T operationModel);

}
