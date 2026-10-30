/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;

import jakarta.inject.Inject;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} which returns a fixed instanced obtained through the constructor. {@link #isSingleton()} always returns
 * {@code true}.
 * <p/>
 * Invocations related to the {@link ZernovaContextAware} and {@link Lifecycle} interfaces are delegated into the {@link #value}
 * object when applies.
 *
 * @param <T>
 * @since 3.7.0
 */
public class ConstantFactoryBean<T> extends AbstractComponent implements FactoryBean<T> {

  @Inject
  private ZernovaContext muleContext;
  private final T value;
  private final boolean inject;

  public ConstantFactoryBean(T value) {
    this(value, false);
  }

  public ConstantFactoryBean(T value, boolean inject) {
    checkArgument(value != null, "value cannot be null");
    this.value = value;
    this.inject = inject;
  }

  @Override
  public T getObject() throws Exception {
    if (value instanceof Component) {
      ((Component) value).setAnnotations(getAnnotations());
    }

    if (inject) {
      muleContext.getInjector().inject(value);
    }

    return value;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public Class<?> getObjectType() {
    return value.getClass();
  }

  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }
}
