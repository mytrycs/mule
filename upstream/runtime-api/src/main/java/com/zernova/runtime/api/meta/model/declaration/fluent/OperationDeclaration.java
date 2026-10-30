/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import com.zernova.runtime.api.meta.model.operation.ExecutionType;
import com.zernova.runtime.api.meta.model.operation.OperationModel;

/**
 * A declaration object for a {@link OperationModel}. It contains raw, unvalidated data which is used to declare the structure of
 * a {@link OperationModel}
 *
 * @since 1.0
 */
public class OperationDeclaration extends ExecutableComponentDeclaration<OperationDeclaration> {

  private boolean blocking = true;
  private ExecutionType executionType = null;

  public OperationDeclaration(String name) {
    super(name);
  }

  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  public ExecutionType getExecutionType() {
    return executionType;
  }

  public void setExecutionType(ExecutionType executionType) {
    this.executionType = executionType;
  }
}
