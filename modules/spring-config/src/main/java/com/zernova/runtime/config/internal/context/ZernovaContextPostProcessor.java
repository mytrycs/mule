/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Responsible for passing in the ZernovaContext instance for all objects in the registry that want it. For an object to get an
 * instance of the ZernovaContext it must implement ZernovaContextAware.
 *
 * @see ZernovaContextAware
 * @see com.zernova.runtime.core.api.ZernovaContext
 *
 * @deprecated this interface is deprecated since {@link ZernovaContext} is deprecated. See {@link ZernovaContext} deprecation
 *             documentation for a replacement.
 */
@Deprecated
public class ZernovaContextPostProcessor implements BeanPostProcessor {

  private final ZernovaContext muleContext;

  public ZernovaContextPostProcessor(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof ZernovaContextAware) {
      if (muleContext == null) {
        return bean;
      }

      ((ZernovaContextAware) bean).setMuleContext(muleContext);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }
}
