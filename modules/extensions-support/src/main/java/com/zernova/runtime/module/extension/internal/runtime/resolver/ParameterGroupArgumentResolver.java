/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkState;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.EventedExecutionContext;
import com.zernova.runtime.module.extension.internal.loader.ParameterGroupDescriptor;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ParameterGroupObjectBuilder;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

public final class ParameterGroupArgumentResolver<T> implements ArgumentResolver<T> {

  private final ParameterGroupObjectBuilder<T> parameterGroupObjectBuilder;

  public ParameterGroupArgumentResolver(ParameterGroupDescriptor group,
                                        ReflectionCache reflectionCache,
                                        ExpressionManager expressionManager) {
    checkState(group.getType().isInstantiable(), "Class %s cannot be instantiated.");
    this.parameterGroupObjectBuilder = new ParameterGroupObjectBuilder<>(group, reflectionCache, expressionManager);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T resolve(ExecutionContext executionContext) {
    try {
      return parameterGroupObjectBuilder.build((EventedExecutionContext) executionContext);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create parameter group"), e);
    }
  }
}
