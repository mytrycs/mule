/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.rx.Exceptions.wrapFatal;
import static com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties.COMPLETION_CALLBACK_CONTEXT_PARAM;

import static java.util.Collections.emptyMap;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.exception.SdkMethodInvocationException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Base class for {@link CompletableComponentExecutor} that use reflection to invoke the actual application logic
 *
 * @since 4.3.0
 */
abstract class AbstractCompletableMethodOperationExecutor<M extends ComponentModel>
    implements CompletableComponentExecutor<M>, OperationArgumentResolverFactory<M>, Lifecycle {

  private static final Logger LOGGER = getLogger(AbstractCompletableMethodOperationExecutor.class);

  protected final GeneratedMethodComponentExecutor<M> executor;

  @Inject
  private ZernovaContext muleContext;

  public AbstractCompletableMethodOperationExecutor(M operationModel, Method operationMethod, Object operationInstance) {
    executor = new GeneratedMethodComponentExecutor<>(operationModel.getParameterGroupModels(),
                                                      operationMethod, operationInstance);
  }

  @Override
  public final void execute(ExecutionContext<M> executionContext, ExecutorCallback callback) {
    try {
      doExecute(executionContext, callback);
    } catch (SdkMethodInvocationException e) {
      handleError(wrapFatal(e.getCause()), (ExecutionContextAdapter<M>) executionContext, callback);
    } catch (Exception e) {
      handleError(e, (ExecutionContextAdapter<M>) executionContext, callback);
    } catch (Throwable t) {
      handleError(wrapFatal(t), (ExecutionContextAdapter<M>) executionContext, callback);
    }
  }

  protected abstract void doExecute(ExecutionContext<M> executionContext, ExecutorCallback callback);

  protected void handleError(Throwable t, ExecutionContextAdapter<M> executionContext, ExecutorCallback callback) {
    CompletionCallback completionCallback = executionContext.getVariable(COMPLETION_CALLBACK_CONTEXT_PARAM);
    if (completionCallback != null) {
      if (t instanceof Exception) {
        completionCallback.error(t);
      } else {
        completionCallback.error(new ZernovaRuntimeException(t));
      }
    } else {
      callback.error(t);
    }
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

  @Override
  public Function<ExecutionContext<M>, Map<String, Object>> createArgumentResolver(M operationModel) {
    return executor instanceof OperationArgumentResolverFactory
        ? executor.createArgumentResolver(operationModel)
        : ec -> emptyMap();
  }

}
