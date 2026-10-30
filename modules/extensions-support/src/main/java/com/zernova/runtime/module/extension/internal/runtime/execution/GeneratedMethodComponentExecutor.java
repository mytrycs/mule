/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.collection.SmallMap.forSize;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.setContextClassLoader;
import static com.zernova.runtime.core.internal.util.CompositeClassLoader.from;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.internal.util.CompositeClassLoader;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.executor.MethodExecutor;
import com.zernova.runtime.module.extension.internal.runtime.exception.SdkMethodInvocationException;
import com.zernova.runtime.module.extension.internal.runtime.execution.executor.MethodExecutorGenerator;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ArgumentResolver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class GeneratedMethodComponentExecutor<M extends ComponentModel>
    implements Lifecycle, OperationArgumentResolverFactory<M> {

  private static class NoArgumentsResolverDelegate implements ArgumentResolverDelegate {

    private static final Supplier[] EMPTY_SUPPLIER = new Supplier[] {};
    private static final Object[] EMPTY = new Object[] {};

    @Override
    public ArgumentResolver<?>[] getArgumentResolvers() {
      return new ArgumentResolver[] {};
    }

    @Override
    public Object[] resolve(ExecutionContext executionContext, Class<?>[] parameterTypes) {
      return EMPTY;
    }

    @Override
    public Supplier<Object>[] resolveDeferred(ExecutionContext executionContext, Class<?>[] parameterTypes) {
      return EMPTY_SUPPLIER;
    }
  }


  private static final Logger LOGGER = getLogger(GeneratedMethodComponentExecutor.class);
  private static final ArgumentResolverDelegate NO_ARGS_DELEGATE = new NoArgumentsResolverDelegate();

  private final List<ParameterGroupModel> groups;
  private final Method method;
  private final Object componentInstance;
  private final ClassLoader extensionClassLoader;

  @Inject
  private MethodExecutorGenerator methodExecutorGenerator;

  private ArgumentResolverDelegate argumentResolverDelegate;
  private MethodExecutor methodExecutor;
  private ZernovaContext muleContext;

  public GeneratedMethodComponentExecutor(List<ParameterGroupModel> groups, Method method, Object componentInstance) {
    this.groups = groups;
    this.method = method;
    this.componentInstance = componentInstance;
    extensionClassLoader = method.getDeclaringClass().getClassLoader();
  }

  public Object execute(ExecutionContext<M> executionContext) {
    try {
      return methodExecutor.execute(executionContext);
    } catch (ZernovaRuntimeException e) {
      throw e;
    } catch (Throwable t) {
      throw new SdkMethodInvocationException(t);
    }
  }

  private Supplier<Object>[] getParameterValues(ExecutionContext<M> executionContext, Class<?>[] parameterTypes) {
    return argumentResolverDelegate.resolveDeferred(executionContext, parameterTypes);
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(componentInstance, true, muleContext);

    argumentResolverDelegate =
        isEmpty(method.getParameterTypes()) ? NO_ARGS_DELEGATE : getMethodArgumentResolver(groups, method);

    try {
      methodExecutor = methodExecutorGenerator.generate(componentInstance, method, argumentResolverDelegate);
    } catch (Exception e) {
      throw new InitialisationException(e, this);
    }
  }

  private ArgumentResolverDelegate getMethodArgumentResolver(List<ParameterGroupModel> groups, Method method) {
    try {
      MethodArgumentResolverDelegate resolver = new MethodArgumentResolverDelegate(groups, method);
      initialiseIfNeeded(resolver, muleContext);
      return resolver;
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not initialize argument resolver resolver"), e);
    }
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(componentInstance);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(componentInstance);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(componentInstance, LOGGER);
  }

  @Inject
  public void setMuleContext(ZernovaContext context) {
    muleContext = context;
  }

  @Override
  public Function<ExecutionContext<M>, Map<String, Object>> createArgumentResolver(M operationModel) {
    return ec -> {
      Thread thread = Thread.currentThread();
      ClassLoader currentClassLoader = thread.getContextClassLoader();
      final CompositeClassLoader compositeClassLoader = from(extensionClassLoader, currentClassLoader);
      setContextClassLoader(thread, currentClassLoader, compositeClassLoader);
      try {
        final Object[] resolved = getParameterValues(ec, method.getParameterTypes());

        int parameterCount = method.getParameterCount();
        final Map<String, Object> resolvedParams = forSize(parameterCount);
        for (int i = 0; i < parameterCount; ++i) {
          resolvedParams.put(method.getParameters()[i].getName(), resolved[i]);
        }
        return resolvedParams;
      } finally {
        setContextClassLoader(thread, compositeClassLoader, currentClassLoader);
      }
    };
  }
}
