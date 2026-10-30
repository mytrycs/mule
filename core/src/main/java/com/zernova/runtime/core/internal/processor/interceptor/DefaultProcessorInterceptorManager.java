/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.interceptor;

import static com.zernova.runtime.api.interception.FlowInterceptorFactory.FLOW_INTERCEPTORS_ORDER_REGISTRY_KEY;
import static com.zernova.runtime.api.interception.ProcessorInterceptorFactory.INTERCEPTORS_ORDER_REGISTRY_KEY;
import static com.zernova.runtime.api.interception.SourceInterceptorFactory.SOURCE_INTERCEPTORS_ORDER_REGISTRY_KEY;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.interception.FlowInterceptorFactory;
import com.zernova.runtime.api.interception.FlowInterceptorFactory.FlowInterceptorOrder;
import com.zernova.runtime.api.interception.ProcessorInterceptorFactory;
import com.zernova.runtime.api.interception.ProcessorInterceptorFactory.ProcessorInterceptorOrder;
import com.zernova.runtime.api.interception.SourceInterceptorFactory;
import com.zernova.runtime.api.interception.SourceInterceptorFactory.SourceInterceptorOrder;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.interception.InterceptorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class DefaultProcessorInterceptorManager implements InterceptorManager, Initialisable {

  @Inject
  private ZernovaContext context;

  private List<ProcessorInterceptorFactory> interceptorFactories = new ArrayList<>();
  private List<FlowInterceptorFactory> flowInterceptorFactories = new ArrayList<>();
  private List<SourceInterceptorFactory> sourceInterceptorFactories = new ArrayList<>();
  private List<String> interceptorsOrder = new ArrayList<>();
  private List<String> flowInterceptorsOrder = new ArrayList<>();
  private List<String> sourceInterceptorsOrder = new ArrayList<>();

  @Override
  @Inject
  @Named(INTERCEPTORS_ORDER_REGISTRY_KEY)
  public void setInterceptorsOrder(Optional<ProcessorInterceptorOrder> packagesOrder) {
    interceptorsOrder = packagesOrder.map(order -> order.get()).orElse(emptyList());
  }

  @Override
  @Inject
  @Named(FLOW_INTERCEPTORS_ORDER_REGISTRY_KEY)
  public void setFlowInterceptorsOrder(Optional<FlowInterceptorOrder> packagesOrder) {
    flowInterceptorsOrder = packagesOrder.map(order -> order.get()).orElse(emptyList());
  }

  @Override
  @Inject
  @Named(SOURCE_INTERCEPTORS_ORDER_REGISTRY_KEY)
  public void setSourceInterceptorsOrder(Optional<SourceInterceptorOrder> packagesOrder) {
    sourceInterceptorsOrder = packagesOrder.map(order -> order.get()).orElse(emptyList());
  }

  @Override
  public void initialise() throws InitialisationException {
    interceptorFactories.forEach(interceptorFactory -> {
      try {
        context.getInjector().inject(interceptorFactory);
      } catch (ZernovaException e) {
        throw new ZernovaRuntimeException(e);
      }
    });
    sourceInterceptorFactories.forEach(interceptorFactory -> {
      try {
        context.getInjector().inject(interceptorFactory);
      } catch (ZernovaException e) {
        throw new ZernovaRuntimeException(e);
      }
    });
  }

  @Override
  @Inject
  public void setInterceptorFactories(Optional<List<ProcessorInterceptorFactory>> interceptorFactories) {
    this.interceptorFactories = interceptorFactories.orElse(emptyList());
  }

  @Override
  @Inject
  public void setFlowInterceptorFactories(Optional<List<FlowInterceptorFactory>> interceptorFactories) {
    this.flowInterceptorFactories = interceptorFactories.orElse(emptyList());
  }

  @Override
  @Inject
  public void setSourceInterceptorFactories(Optional<List<SourceInterceptorFactory>> interceptorFactories) {
    this.sourceInterceptorFactories = interceptorFactories.orElse(emptyList());
  }

  @Override
  public List<ProcessorInterceptorFactory> getInterceptorFactories() {
    final List<ProcessorInterceptorFactory> sortedInterceptors = new ArrayList<>(interceptorFactories);

    sortedInterceptors.sort((o1, o2) -> orderIndexOf(o1) - orderIndexOf(o2));

    return unmodifiableList(sortedInterceptors);
  }

  private int orderIndexOf(ProcessorInterceptorFactory factory) {
    int i = 0;
    for (String interceptorsOrderItem : interceptorsOrder) {
      if (factory.getClass().getName().startsWith(interceptorsOrderItem)) {
        return i;
      }
      ++i;
    }
    return MAX_VALUE;
  }

  @Override
  public List<FlowInterceptorFactory> getFlowInterceptorFactories() {
    final List<FlowInterceptorFactory> sortedInterceptors = new ArrayList<>(flowInterceptorFactories);

    sortedInterceptors.sort((o1, o2) -> orderIndexOf(o1) - orderIndexOf(o2));

    return unmodifiableList(sortedInterceptors);
  }

  private int orderIndexOf(FlowInterceptorFactory factory) {
    int i = 0;
    for (String interceptorsOrderItem : flowInterceptorsOrder) {
      if (factory.getClass().getName().startsWith(interceptorsOrderItem)) {
        return i;
      }
      ++i;
    }
    return MAX_VALUE;
  }

  @Override
  public List<SourceInterceptorFactory> getSourceInterceptorFactories() {
    final List<SourceInterceptorFactory> sortedInterceptors = new ArrayList<>(sourceInterceptorFactories);

    sortedInterceptors.sort((o1, o2) -> orderIndexOf(o1) - orderIndexOf(o2));

    return unmodifiableList(sortedInterceptors);
  }

  private int orderIndexOf(SourceInterceptorFactory factory) {
    int i = 0;
    for (String interceptorsOrderItem : sourceInterceptorsOrder) {
      if (factory.getClass().getName().startsWith(interceptorsOrderItem)) {
        return i;
      }
      ++i;
    }
    return MAX_VALUE;
  }
}
