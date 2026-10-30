/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.construct;

import com.zernova.runtime.api.component.execution.ExecutableComponent;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;

import java.util.List;

/**
 * A strongly typed operation defined through the Mule language.
 *
 * @since 4.5.0
 */
public interface Operation extends ExecutableComponent, Lifecycle {


  /**
   * @return the model that describes {@code this} operation
   */
  OperationModel getModel();

  /**
   * Creates instances of {@link Operation} with a default implementation
   * <p>
   * Builder instances can be configured until {@link #build()} is called. After that point, builder methods will fail to update
   * the builder state.
   *
   * @since 4.5.0
   */
  interface Builder {

    /**
     * Configures the message processors to execute as part of the operation.
     *
     * @param processors message processors to execute. Non null.
     * @return same builder instance.
     */
    Builder processors(List<Processor> processors);

    /**
     * Configures the message processors to execute as part of the operation.
     *
     * @param processors message processors to execute.
     * @return {@code this} builder
     */
    Builder processors(Processor... processors);

    /**
     * Configures the model that represents the produced operation
     *
     * @param operationModel an {@link OperationModel}
     * @return {@code this} builder
     */
    Builder setOperationModel(OperationModel operationModel);

    /**
     * Configures the {@link ZernovaContext} that owns the operation
     *
     * @param muleContext a {@link ZernovaContext}
     * @return {@code this} builder
     */
    Builder setMuleContext(ZernovaContext muleContext);

    /**
     * Builds an {@link Operation} with the provided configuration.
     *
     * @return a new {@link Operation} instance.
     * @throws IllegalStateException if operation model, processors or muleContext not set.
     */
    Operation build();
  }
}
