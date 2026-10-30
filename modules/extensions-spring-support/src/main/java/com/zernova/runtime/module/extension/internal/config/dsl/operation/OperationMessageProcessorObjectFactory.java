/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.operation;

import static java.util.Collections.emptyList;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.exception.EnrichedErrorMapping;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.config.dsl.ComponentMessageProcessorObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.operation.OperationMessageProcessor;
import com.zernova.runtime.module.extension.internal.runtime.operation.OperationMessageProcessorBuilder;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;

import jakarta.inject.Inject;

/**
 * An {@link AbstractExtensionObjectFactory} which produces {@link OperationMessageProcessor} instances
 *
 * @since 4.0
 */
public class OperationMessageProcessorObjectFactory
    extends ComponentMessageProcessorObjectFactory<OperationModel, OperationMessageProcessor> {

  private List<EnrichedErrorMapping> errorMappings = emptyList();

  @Inject
  private ReflectionCache reflectionCache;
  @Inject
  private ExpressionManager expressionManager;
  @Inject
  private ExtensionConnectionSupplier extensionConnectionSupplier;
  @Inject
  private ComponentTracerFactory<CoreEvent> componentTracerFactory;

  public OperationMessageProcessorObjectFactory(ExtensionModel extensionModel,
                                                OperationModel componentModel,
                                                ZernovaContext muleContext) {
    super(extensionModel, componentModel, muleContext);
  }

  @Override
  protected OperationMessageProcessorBuilder getMessageProcessorBuilder() {
    return new OperationMessageProcessorBuilder(extensionModel, componentModel, errorMappings,
                                                reflectionCache,
                                                expressionManager,
                                                extensionConnectionSupplier,
                                                componentTracerFactory,
                                                muleContext);
  }

  public void setErrorMappings(List<EnrichedErrorMapping> errorMappings) {
    this.errorMappings = errorMappings;
  }
}
