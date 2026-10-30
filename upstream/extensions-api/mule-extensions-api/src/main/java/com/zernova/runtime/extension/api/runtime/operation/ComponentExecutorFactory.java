/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.operation;

import com.zernova.runtime.api.meta.model.ComponentModel;

import java.util.Map;

/**
 * Creates {@link ComponentExecutor} instances
 *
 * @since 1.0
 * @deprecated since 1.3.0. Use {@link CompletableComponentExecutorFactory} instead
 */
@Deprecated
public interface ComponentExecutorFactory<T extends ComponentModel> {

  /**
   * Creates a new {@link ComponentExecutor}
   *
   * @param componentModel the model of the component to be executed
   * @param parameters     parameters for initializing the executor
   * @return a new {@link ComponentExecutor}
   */
  ComponentExecutor<T> createExecutor(T componentModel, Map<String, Object> parameters);
}
