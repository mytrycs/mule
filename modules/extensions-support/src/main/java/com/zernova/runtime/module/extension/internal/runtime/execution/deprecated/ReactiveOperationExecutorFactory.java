/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution.deprecated;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.getInitialiserEvent;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutorFactory;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * An implementation of {@link ComponentExecutorFactory} which produces instances of {@link ComponentExecutorFactory}.
 *
 * @param <T> the type of the class in which the implementing method is declared
 * @since 3.7.0
 * @deprecated since 4.3
 */
@Deprecated
public final class ReactiveOperationExecutorFactory<T, M extends ComponentModel>
    implements ComponentExecutorFactory<M> {

  private final Class<T> implementationClass;
  private final Method operationMethod;

  public ReactiveOperationExecutorFactory(Class<T> implementationClass, Method operationMethod) {
    checkArgument(implementationClass != null, "implementationClass cannot be null");
    checkArgument(operationMethod != null, "operationMethod cannot be null");

    this.implementationClass = implementationClass;
    this.operationMethod = operationMethod;
  }

  @Override
  public ComponentExecutor<M> createExecutor(M operationModel, Map<String, Object> parameters) {
    DefaultObjectBuilder objectBuilder = new DefaultObjectBuilder(implementationClass, new ReflectionCache());
    parameters.forEach((k, v) -> objectBuilder.addPropertyResolver(k, new StaticValueResolver<>(v)));
    Object delegate;
    CoreEvent initialiserEvent = null;
    try {
      initialiserEvent = getInitialiserEvent();
      delegate = objectBuilder.build(ValueResolvingContext.builder(initialiserEvent).build());
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of operation class "
          + implementationClass.getName()), e);
    } finally {
      if (initialiserEvent != null) {
        ((BaseEventContext) initialiserEvent.getContext()).success();
      }
    }

    return new ReactiveMethodOperationExecutor(operationModel, operationMethod, delegate);
  }
}
