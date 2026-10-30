/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.interceptor;

import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.FLOW;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.interception.FlowInterceptorFactory;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;

public class FlowInterceptorFactoryAdapter implements ComponentInterceptorFactoryAdapter {

  private final FlowInterceptorFactory interceptorFactory;

  public FlowInterceptorFactoryAdapter(FlowInterceptorFactory interceptorFactory) {
    this.interceptorFactory = interceptorFactory;
  }

  @Override
  public boolean isInterceptable(ReactiveProcessor component) {
    return true;
  }

  @Override
  public boolean intercept(ComponentLocation componentLocation) {
    return componentLocation.getComponentIdentifier().getType() == FLOW
        && interceptorFactory.intercept(componentLocation.getRootContainerName());
  }

  @Override
  public ComponentInterceptorAdapter get() {
    return new FlowInterceptorWrapper(interceptorFactory.get());
  }
}
