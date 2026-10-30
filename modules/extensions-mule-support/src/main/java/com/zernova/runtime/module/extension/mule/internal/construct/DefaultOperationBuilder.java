/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.construct;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.api.util.Preconditions.checkState;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getDefaultProcessingStrategyFactory;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;

import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.module.extension.mule.internal.construct.Operation.Builder;

import java.util.List;

/**
 * Default {@link Builder} implementation
 *
 * @since 4.5.0
 */
class DefaultOperationBuilder implements Builder {

  private OperationModel operationModel;
  private ZernovaContext muleContext;
  private List<Processor> processors = emptyList();

  private ZernovaOperation product;


  /**
   * {@inheritDoc}
   */
  @Override
  public Builder processors(List<Processor> processors) {
    checkImmutable();
    checkArgument(processors != null, "processors cannot be null");
    this.processors = processors;

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Builder processors(Processor... processors) {
    checkImmutable();
    this.processors = asList(processors);

    return this;
  }

  @Override
  public Builder setOperationModel(OperationModel operationModel) {
    this.operationModel = operationModel;
    return this;
  }

  @Override
  public Builder setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
    return this;
  }

  /**
   * Builds a flow with the provided configuration.
   *
   * @return a new flow instance.
   */
  @Override
  public Operation build() {
    checkImmutable();
    checkInvoked(operationModel, "setOperationModel(OperationModel)");
    checkInvoked(muleContext, "setMuleContext(ZernovaContext)");
    checkState(processors != null && !processors.isEmpty(), "Processors cannot be null nor empty");

    product = new ZernovaOperation(processors,
                                operationModel,
                                muleContext);

    return product;
  }

  private final void checkImmutable() {
    if (product != null) {
      throw new IllegalStateException("Cannot change attributes once the operation was built");
    }
  }

  private void checkInvoked(Object value, String methodName) {
    checkState(value != null, methodName + " not invoked");
  }
}
