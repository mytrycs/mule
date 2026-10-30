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
import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Mono.from;
import static reactor.core.publisher.Mono.just;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutorFactory;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ComponentExecutorFactory;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.execution.OperationArgumentResolverFactory;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;

/**
 * Adapts a legacy {@link ComponentExecutorFactory} into a {@link CompletableComponentExecutorFactory}.
 * <p>
 * The produced {@link CompletableComponentExecutor} instances will support mapping all the {@link Lifecycle} interfaces to the
 * adapted {@link ComponentExecutor}
 *
 * @since 4.3.0
 */
public class ComponentExecutorCompletableAdapterFactory<T extends ComponentModel>
    implements CompletableComponentExecutorFactory<T> {

  private static final Logger LOGGER = getLogger(ComponentExecutorCompletableAdapterFactory.class);

  private final ComponentExecutorFactory<T> delegate;

  public ComponentExecutorCompletableAdapterFactory(ComponentExecutorFactory<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableComponentExecutor<T> createExecutor(T componentModel, Map<String, Object> parameters) {
    return new ComponentExecutorCompletableAdapter<>(delegate.createExecutor(componentModel, parameters));
  }

  private static class ComponentExecutorCompletableAdapter<T extends ComponentModel> implements CompletableComponentExecutor<T>,
      OperationArgumentResolverFactory<T>, Lifecycle, ZernovaContextAware {

    private final ComponentExecutor<T> delegate;
    private ZernovaContext muleContext;

    ComponentExecutorCompletableAdapter(ComponentExecutor<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void execute(ExecutionContext<T> executionContext, ExecutorCallback callback) {
      try {
        from(delegate.execute(executionContext))
            .switchIfEmpty(just(""))
            .subscribe(callback::complete, callback::error);
      } catch (Throwable t) {
        callback.error(t);
      }
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
    public Function<ExecutionContext<T>, Map<String, Object>> createArgumentResolver(T operationModel) {
      return delegate instanceof OperationArgumentResolverFactory
          ? ((OperationArgumentResolverFactory<T>) delegate).createArgumentResolver(operationModel)
          : ec -> emptyMap();
    }
  }
}
