/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isPagedOperation;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getPagingResultTransformer;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.supportsOAuth;
import static com.zernova.runtime.tracer.customization.api.InternalSpanNames.GET_CONNECTION_SPAN_NAME;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.internal.exception.EnrichedErrorMapping;
import com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.streaming.PagingResultTransformer;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;

/**
 * Provides instances of {@link OperationMessageProcessor} for a given {@link OperationModel}
 *
 * @since 4.0
 */
public final class OperationMessageProcessorBuilder
    extends ComponentMessageProcessorBuilder<OperationModel, OperationMessageProcessor> {

  private final List<EnrichedErrorMapping> errorMappings;

  private final ComponentTracerFactory<CoreEvent> componentTracerFactory;

  public OperationMessageProcessorBuilder(ExtensionModel extension,
                                          OperationModel operation,
                                          List<EnrichedErrorMapping> errorMappings,
                                          ReflectionCache reflectionCache,
                                          ExpressionManager expressionManager,
                                          ExtensionConnectionSupplier extensionConnectionSupplier,
                                          ComponentTracerFactory<CoreEvent> componentTracerFactory,
                                          ZernovaContext muleContext) {

    super(extension, operation,
          reflectionCache,
          expressionManager,
          extensionConnectionSupplier,
          muleContext);

    this.errorMappings = errorMappings;
    this.componentTracerFactory = componentTracerFactory;
  }

  @Override
  protected OperationMessageProcessor createMessageProcessor(ExtensionManager extensionManager, ResolverSet arguments) {
    ValueResolver<ConfigurationProvider> configurationProviderResolver = getConfigurationProviderResolver();
    ResultTransformer resultTransformer = null;

    OperationMessageProcessor operationMessageProcessor = null;

    final boolean supportsOAuth = supportsOAuth(extensionModel);
    boolean isPagedOperation = isPagedOperation(operationModel);

    if (isPagedOperation) {
      resultTransformer = getPagingResultTransformer(operationModel, extensionConnectionSupplier, supportsOAuth,
                                                     DummyComponentTracerFactory.DUMMY_COMPONENT_TRACER_INSTANCE)
                                                         .orElse(null);
    }

    if (supportsOAuth) {
      operationMessageProcessor =
          new OAuthOperationMessageProcessor(extensionModel, operationModel, configurationProviderResolver, target,
                                             targetValue,
                                             errorMappings, arguments, cursorProviderFactory, retryPolicyTemplate,
                                             nestedChain, classLoader,
                                             extensionManager, reflectionCache, resultTransformer,
                                             terminationTimeout);
    } else {
      operationMessageProcessor =
          new OperationMessageProcessor(extensionModel, operationModel, configurationProviderResolver, target, targetValue,
                                        errorMappings, arguments, cursorProviderFactory, retryPolicyTemplate, nestedChain,
                                        classLoader, extensionManager, reflectionCache, resultTransformer,
                                        terminationTimeout);
    }

    // TODO: Make the initial span info non Component dependant (mainly component location but must be think as a general issue)
    if (isPagedOperation && resultTransformer != null) {
      ((PagingResultTransformer) resultTransformer).setOperationConnectionTracer(
                                                                                 componentTracerFactory
                                                                                     .fromComponent(operationMessageProcessor,
                                                                                                    GET_CONNECTION_SPAN_NAME,
                                                                                                    ""));
    }

    return operationMessageProcessor;
  }
}
