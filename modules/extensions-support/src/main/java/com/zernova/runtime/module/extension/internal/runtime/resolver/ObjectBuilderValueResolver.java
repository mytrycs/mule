/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingException;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilder;

import java.util.Map;

/**
 * A {@link ValueResolver} which wraps an {@link ObjectBuilder} and calls {@link ObjectBuilder#build(ValueResolvingContext)} on
 * each {@link #resolve(ValueResolvingContext)}.
 * <p/>
 * It implements {@link Lifecycle} and propagates all lifecycle events to the underlying {@code builder}
 *
 * @param <T> the generic type for the instances built.
 * @since 3.7.0
 */
public class ObjectBuilderValueResolver<T> extends AbstractComponent
    implements ValueResolver<T>, Initialisable, ParameterValueResolver {

  protected final ZernovaContext muleContext;
  private final ObjectBuilder<T> builder;

  public ObjectBuilderValueResolver(ObjectBuilder<T> builder, ZernovaContext muleContext) {
    checkArgument(builder != null, "builder cannot be null");
    this.builder = builder;
    this.muleContext = muleContext;
  }

  /**
   * Delegates to {@code builder}
   *
   * @param context a {@link ValueResolvingContext}
   * @return the {@code builder}'s output object
   * @throws ZernovaException
   */
  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    return builder.build(context);
  }

  /**
   * @return {@code true} if {@code builder} is dynamic
   */
  @Override
  public boolean isDynamic() {
    return builder.isDynamic();
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(builder, true, muleContext);
  }

  /**
   * {@inheritDoc}
   *
   * @since 4.1.1
   */
  @Override
  public Object getParameterValue(String parameterName) throws ValueResolvingException {
    if (builder instanceof ParameterValueResolver) {
      return ((ParameterValueResolver) builder).getParameterValue(parameterName);
    } else {
      throw new ValueResolvingException(format("Unable to resolve value for parameter '%s'", parameterName));
    }
  }

  @Override
  public Map<String, ValueResolver<? extends Object>> getParameters() throws ValueResolvingException {
    if (builder instanceof ParameterValueResolver) {
      return unmodifiableMap(((ParameterValueResolver) builder).getParameters());
    } else {
      return emptyMap();
    }
  }
}
