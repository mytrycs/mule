/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverUtils.resolveRecursively;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

/**
 * {@link ValueResolver} implementation for {@link TypedValue} that are not resolved from an expression.
 * <p>
 * This {@link TypedValueValueResolverWrapper} delegates the resolution to the given {@link ValueResolver} from the constructor.
 *
 * @since 4.0
 * @see TypedValue
 */
public final class TypedValueValueResolverWrapper<T> implements ValueResolver<TypedValue<T>>, Initialisable, ZernovaContextAware {

  private final ValueResolver resolver;

  ZernovaContext muleContext;

  public TypedValueValueResolverWrapper(ValueResolver resolver) {
    this.resolver = resolver;
  }

  /**
   * Resolves the value of {@link this#resolver} using the given {@link CoreEvent} and returns the correspondent
   * {@link TypedValue}
   *
   * @param context a {@link ValueResolvingContext} to resolve the {@link ValueResolver}
   * @return The {@link TypedValue} of the resolved value
   * @throws ZernovaException if it fails to resolve the value
   */
  @Override
  public TypedValue<T> resolve(ValueResolvingContext context) throws ZernovaException {
    return TypedValue.of((T) resolveRecursively(resolver, context));
  }

  /**
   * {@inheritDoc}
   */
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
