/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.operation;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.ComponentModelVisitor;
import com.zernova.runtime.api.meta.model.ConnectableComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.notification.HasNotifications;

/**
 * A definition of an operation in a {@link ExtensionModel}.
 * <p>
 * Operation models implement the flyweight pattern. This means that a given operation should only be represented by only one
 * instance of this class. Thus, if the same operation is contained by different {@link HasOperationModels} instances, then each
 * of those containers should reference the same operation model instance.
 *
 * @since 1.0
 */
@NoImplement
public interface OperationModel extends ConnectableComponentModel, HasNotifications {

  /**
   * @return Whether this operation is blocking or non blocking execution is supported
   */
  boolean isBlocking();

  /**
   * @return this operation's {@link ExecutionType}
   */
  ExecutionType getExecutionType();

  /**
   * {@inheritDoc}
   */
  @Override
  default void accept(ComponentModelVisitor visitor) {
    visitor.visit(this);
  }
}
