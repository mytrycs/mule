/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.construct;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.config.dsl.ComponentMessageProcessorObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.operation.ConstructMessageProcessor;
import com.zernova.runtime.module.extension.internal.runtime.operation.ConstructMessageProcessorBuilder;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import jakarta.inject.Inject;

/**
 * An {@link AbstractExtensionObjectFactory} which produces {@link ConstructMessageProcessor} instances
 *
 * @since 4.0
 */
public class ConstructMessageProcessorObjectFactory
    extends ComponentMessageProcessorObjectFactory<ConstructModel, ConstructMessageProcessor> {

  @Inject
  private ReflectionCache reflectionCache;
  @Inject
  private ExpressionManager expressionManager;
  @Inject
  private ExtensionConnectionSupplier extensionConnectionSupplier;
  @Inject
  private ComponentTracerFactory<CoreEvent> componentTracerFactory;

  public ConstructMessageProcessorObjectFactory(ExtensionModel extensionModel,
                                                ConstructModel componentModel,
                                                ZernovaContext muleContext) {
    super(extensionModel, componentModel, muleContext);
  }

  @Override
  protected ConstructMessageProcessorBuilder getMessageProcessorBuilder() {
    return new ConstructMessageProcessorBuilder(extensionModel, componentModel,
                                                reflectionCache,
                                                expressionManager,
                                                extensionConnectionSupplier,
                                                componentTracerFactory,
                                                muleContext);
  }

}
