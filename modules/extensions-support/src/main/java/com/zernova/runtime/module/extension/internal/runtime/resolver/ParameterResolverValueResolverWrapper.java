/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverUtils.resolveRecursively;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@link ValueResolver} implementation for {@link ParameterResolver} that wraps another {@link ValueResolver}
 *
 * @since 4.0
 * @see ParameterResolver
 */
public class ParameterResolverValueResolverWrapper<T>
    implements ValueResolver<ParameterResolver<T>>, Initialisable, ZernovaContextAware {

  private ValueResolver<T> resolver;
  private ZernovaContext muleContext;
  private final Reference<Function<ValueResolvingContext, ParameterResolver>> parameterResolverSupplier = new Reference<>();

  public ParameterResolverValueResolverWrapper(ValueResolver<T> resolver) {
    this.resolver = resolver;
    Function<ValueResolvingContext, ParameterResolver> parameterResolverFactory = (context) -> new ParameterResolver<T>() {

      @Override
      public T resolve() {
        try {
          return resolveRecursively(resolver, context);
        } catch (ZernovaException e) {
          throw new ZernovaRuntimeException(e);
        }
      }

      @Override
      public Optional<String> getExpression() {
        return resolver instanceof ExpressionBasedValueResolver
            ? ofNullable(((ExpressionBasedValueResolver) resolver).getExpression())
            : empty();
      }
    };

    if (resolver.isDynamic()) {
      parameterResolverSupplier.set(parameterResolverFactory);
    } else {
      parameterResolverSupplier.set(context -> {
        ParameterResolver staticResolver = parameterResolverFactory.apply(context);
        Function<ValueResolvingContext, ParameterResolver> valueResolvingContextParameterResolverFunction =
            (ctx) -> staticResolver;
        parameterResolverSupplier.set(valueResolvingContextParameterResolverFunction);
        return staticResolver;
      });
    }
  }

  /**
   * Resolves the value of {@link this#resolver} using the given {@link CoreEvent} and wraps it into a
   * {@link StaticParameterResolver}
   *
   * @param context a {@link ValueResolvingContext} to resolve the {@link ValueResolver}
   * @return an {@link ParameterResolver} with the resolved value
   * @throws ZernovaException if it fails to resolve the value
   */
  @Override
  public ParameterResolver<T> resolve(ValueResolvingContext context) throws ZernovaException {
    return parameterResolverSupplier.get().apply(context);
  }

  @Override
  public boolean isDynamic() {
    return resolver.isDynamic();
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(resolver, true, muleContext);
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }
}
