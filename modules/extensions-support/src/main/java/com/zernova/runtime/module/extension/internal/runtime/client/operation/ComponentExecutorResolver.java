/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.operation;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.module.extension.internal.runtime.client.NullComponent.NULL_COMPONENT;
import static com.zernova.runtime.module.extension.internal.runtime.execution.CompletableOperationExecutorFactory.extractExecutorInitialisationParams;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getOperationExecutorFactory;

import static java.util.Optional.empty;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutorFactory;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the appropriate {@link CompletableComponentExecutor} for a given {@link OperationKey}
 *
 * @since 4.5.0
 */
abstract class ComponentExecutorResolver {

  /**
   * Factory method to obtain instances of this class
   */
  static CompletableComponentExecutor<OperationModel> from(OperationKey key,
                                                           ExtensionManager extensionManager,
                                                           ExpressionManager expressionManager,
                                                           ReflectionCache reflectionCache) {
    final OperationModel operationModel = key.getOperationModel();

    CompletableComponentExecutorFactory<OperationModel> operationExecutorFactory = getOperationExecutorFactory(operationModel);
    Map<String, Object> initParams = new HashMap<>();
    try {
      initParams.putAll(extractExecutorInitialisationParams(
                                                            key.getExtensionModel(),
                                                            operationModel,
                                                            initParams,
                                                            NULL_COMPONENT,
                                                            empty(),
                                                            extensionManager,
                                                            expressionManager,
                                                            reflectionCache));
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage(
                                                         "Exception found resolving parameters for operation client: "
                                                             + e.getMessage()),
                                     e);
    }

    return operationExecutorFactory.createExecutor(operationModel, initParams);
  }
}
