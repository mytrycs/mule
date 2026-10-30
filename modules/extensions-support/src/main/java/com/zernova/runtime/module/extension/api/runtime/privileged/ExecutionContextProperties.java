/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.runtime.privileged;


import static com.zernova.runtime.core.api.config.ZernovaProperties.PROPERTY_PREFIX;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;

/**
 * Constants used for information related to {@link com.zernova.runtime.extension.api.runtime.operation.ExecutionContext}
 *
 * @since 4.1
 */
public class ExecutionContextProperties {

  /**
   * The key of an execution context variable which holds the {@link CompletionCallback} that a non blocking component will use to
   * notify completion or failure
   */
  public static final String COMPLETION_CALLBACK_CONTEXT_PARAM = PROPERTY_PREFIX + "COMPLETION_CALLBACK_CONTEXT_PARAM";

  private ExecutionContextProperties() {}

}
