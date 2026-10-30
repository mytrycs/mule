/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.loader.java.property;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutorFactory;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutorFactory;

/**
 * A {@link ModelProperty} meant to be used on {@link ComponentModel component models}, which provides access to a
 * {@link ComponentExecutorFactory} used to execute such component
 *
 * @since 4.3
 */
public final class CompletableComponentExecutorModelProperty implements ModelProperty {

  private final CompletableComponentExecutorFactory executorFactory;

  /**
   * Creates a new instance
   *
   * @param executorFactory a {@link ComponentExecutorFactory}
   */
  public CompletableComponentExecutorModelProperty(CompletableComponentExecutorFactory executorFactory) {
    this.executorFactory = executorFactory;
  }

  /**
   * @return a {@link CompletableComponentExecutorFactory}
   */
  public CompletableComponentExecutorFactory getExecutorFactory() {
    return executorFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return null;
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
