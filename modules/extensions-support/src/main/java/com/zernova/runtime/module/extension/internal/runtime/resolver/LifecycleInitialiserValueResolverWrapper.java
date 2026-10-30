/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.util.Collections.newSetFromMap;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.lifecycle.LifecycleState;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link LifecycleAwareValueResolverWrapper} which depending on the lifecycle state of the muleContext, applies
 * the Initialise and Start phases on the returned objects.
 * <p>
 * This resolver keeps track of all the returned values, making sure that lifecycle is only applied once on each possible return
 * value.
 *
 * @param <T> the generic type of the returned values
 * @since 4.1
 */
public class LifecycleInitialiserValueResolverWrapper<T> extends LifecycleAwareValueResolverWrapper<T> {

  private final ZernovaContext muleContext;
  private final Set<Reference<Object>> producedObjects = newSetFromMap(new ConcurrentHashMap<>());

  /**
   * Creates a new instance
   *
   * @param delegate    the delegate resolver
   * @param muleContext the current {@link ZernovaContext}
   */
  public LifecycleInitialiserValueResolverWrapper(ValueResolver<T> delegate, ZernovaContext muleContext) {
    super(delegate);
    this.muleContext = muleContext;
  }

  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    T value = super.resolve(context);

    if (producedObjects.add(new Reference<>(value))) {
      LifecycleState state = muleContext.getLifecycleManager().getState();

      if (state.isInitialising() || state.isInitialised()) {
        initialiseIfNeeded(value, muleContext);
      }

      if (state.isStarting() || state.isStarted()) {
        startIfNeeded(value);
      }
    }

    return value;
  }

  @Override
  public void dispose() {
    producedObjects.clear();
    super.dispose();
  }
}
