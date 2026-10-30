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

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutorFactory;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.execution.OperationArgumentResolverFactory;

import java.util.Map;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

/**
 * Decorates an {@link ComponentExecutor} adding the necessary logic to execute non blocking operations.
 * <p>
 * If the operation being executed is blocking, then it delegates to the wrapped executor transparently. If the operation is non
 * blocking, then it creates and injects a {@link CompletionCallback} on which the operation result will be notified.
 * <p>
 * It also implements {@link Lifecycle} and {@link ZernovaContextAware}, propagating those to the decoratee if necessary
 *
 * @since 4.0
 * @deprecated since 4.3. Use {@link CompletableComponentExecutorFactory} instead
 */
public final class ReactiveOperationExecutionWrapper<M extends ComponentModel>
    implements ComponentExecutor<M>, OperationArgumentResolverFactory<M>, Lifecycle, ZernovaContextAware {

  private static final Logger LOGGER = getLogger(ReactiveOperationExecutionWrapper.class);

  private final ComponentExecutor<M> delegate;
  private ZernovaContext muleContext;

  public ReactiveOperationExecutionWrapper(ComponentExecutor delegate) {
    this.delegate = delegate;
  }

  @Override
  public Publisher<Object> execute(ExecutionContext<M> executionContext) {
    ExecutionContextAdapter<M> context = (ExecutionContextAdapter<M>) executionContext;
    return Mono.create(sink -> {
      MonoCompletionCallback callback = new MonoCompletionCallback(sink);
      context.setVariable(COMPLETION_CALLBACK_CONTEXT_PARAM, callback);

      try {
        delegate.execute(executionContext);
      } catch (Throwable t) {
        sink.error(wrapFatal(t));
      }
    });
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(delegate, true, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(delegate);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(delegate);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(delegate, LOGGER);
  }

  @Override
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  @Override
  public Function<ExecutionContext<M>, Map<String, Object>> createArgumentResolver(M operationModel) {
    return delegate instanceof OperationArgumentResolverFactory
        ? ((OperationArgumentResolverFactory) delegate).createArgumentResolver(operationModel)
        : ec -> emptyMap();
  }
}
