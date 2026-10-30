/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.interceptor;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.interception.ProcessorInterceptorFactory;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;

public class ProcessorInterceptorFactoryAdapter implements ComponentInterceptorFactoryAdapter {

  private final ProcessorInterceptorFactory interceptorFactory;

  public ProcessorInterceptorFactoryAdapter(ProcessorInterceptorFactory interceptorFactory) {
    this.interceptorFactory = interceptorFactory;
  }

  @Override
  public boolean isInterceptable(ReactiveProcessor component) {
    return component instanceof Component
        && ((Component) component).getLocation() != null;
  }

  @Override
  public boolean intercept(ComponentLocation componentLocation) {
    return interceptorFactory.intercept(componentLocation);
  }

  @Override
  public ComponentInterceptorAdapter get() {
    return new ProcessorInterceptorWrapper(interceptorFactory.get());
  }

}
