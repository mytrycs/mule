/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution.deprecated;

import static java.util.Collections.emptyMap;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.rx.Exceptions.wrapFatal;
import static com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties.COMPLETION_CALLBACK_CONTEXT_PARAM;
import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.justOrEmpty;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.execution.GeneratedMethodComponentExecutor;
import com.zernova.runtime.module.extension.internal.runtime.execution.OperationArgumentResolverFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;

import jakarta.inject.Inject;

/**
 * Implementation of {@link ComponentExecutor} which works by using reflection to invoke a method from a class.
 *
 * @since 3.7.0
 * @deprecated since 4.3
 */
@Deprecated
public class ReactiveMethodOperationExecutor<M extends ComponentModel>
    implements ComponentExecutor<M>, OperationArgumentResolverFactory<M>, Lifecycle {

  private static final Logger LOGGER = getLogger(ReactiveMethodOperationExecutor.class);

  private final GeneratedMethodComponentExecutor<M> executor;
  private ZernovaContext muleContext;

  public ReactiveMethodOperationExecutor(M operationModel, Method operationMethod, Object operationInstance) {
    executor =
        new GeneratedMethodComponentExecutor<>(operationModel.getParameterGroupModels(), operationMethod,
                                               operationInstance);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Publisher<Object> execute(ExecutionContext<M> executionContext) {
    try {
      return justOrEmpty(executor.execute(executionContext));
    } catch (Exception e) {
      return handleError(e, (ExecutionContextAdapter<M>) executionContext);
    } catch (Throwable t) {
      return handleError(wrapFatal(t), (ExecutionContextAdapter<M>) executionContext);
    }
  }

  private Publisher<Object> handleError(Throwable t, ExecutionContextAdapter<M> executionContext) {
    CompletionCallback completionCallback = executionContext.getVariable(COMPLETION_CALLBACK_CONTEXT_PARAM);
    if (completionCallback != null) {
      if (t instanceof Exception) {
        completionCallback.error(t);
      } else {
        completionCallback.error(new ZernovaRuntimeException(t));
      }
    }

    return error(t);
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(executor, true, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(executor);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(executor);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(executor, LOGGER);
  }

  @Inject
  public void setMuleContext(ZernovaContext context) {
    muleContext = context;
    executor.setMuleContext(muleContext);
  }

  @Override
  public Function<ExecutionContext<M>, Map<String, Object>> createArgumentResolver(M operationModel) {
    return executor instanceof OperationArgumentResolverFactory
        ? executor.createArgumentResolver(operationModel)
        : ec -> emptyMap();
  }
}
