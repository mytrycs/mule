/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

/**
 * Provides instances of {@link ConstructMessageProcessor} for a given {@link ConstructModel}
 *
 * @since 4.0
 */
public final class ConstructMessageProcessorBuilder
    extends ComponentMessageProcessorBuilder<ConstructModel, ConstructMessageProcessor> {

  public ConstructMessageProcessorBuilder(ExtensionModel extensionModel,
                                          ConstructModel operationModel,
                                          ReflectionCache reflectionCache,
                                          ExpressionManager expressionManager,
                                          ExtensionConnectionSupplier extensionConnectionSupplier,
                                          ComponentTracerFactory<CoreEvent> componentTracerFactory,
                                          ZernovaContext muleContext) {
    super(extensionModel, operationModel,
          reflectionCache, expressionManager, extensionConnectionSupplier,
          muleContext);
  }

  @Override
  protected ConstructMessageProcessor createMessageProcessor(ExtensionManager extensionManager, ResolverSet arguments) {
    return new ConstructMessageProcessor(extensionModel, operationModel,
                                         getConfigurationProviderResolver(), target, targetValue,
                                         arguments,
                                         cursorProviderFactory, retryPolicyTemplate, nestedChain, classLoader,
                                         extensionManager,
                                         reflectionCache,
                                         terminationTimeout);
  }

}
