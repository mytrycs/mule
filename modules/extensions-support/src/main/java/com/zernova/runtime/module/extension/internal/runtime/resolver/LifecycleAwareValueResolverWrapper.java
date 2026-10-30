/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link ValueResolver} wrappers.
 * <p>
 * It propagates {@link Lifecycle} phases and DI to the wrapped resolver.
 *
 * @param <T> the generic type of the resolved values
 * @since 4.0
 */
public class LifecycleAwareValueResolverWrapper<T> implements ValueResolver<T>, Lifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleAwareValueResolverWrapper.class);

  protected final ValueResolver<T> delegate;

  @Inject
  protected ZernovaContext muleContext;

  public LifecycleAwareValueResolverWrapper(ValueResolver<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public T resolve(ValueResolvingContext context) throws ZernovaException {
    return delegate.resolve(context);
  }

  @Override
  public boolean isDynamic() {
    return delegate.isDynamic();
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

}
