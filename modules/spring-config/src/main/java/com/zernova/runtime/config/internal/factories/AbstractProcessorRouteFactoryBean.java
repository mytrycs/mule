/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;
import static com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder.newLazyProcessorChainBuilder;

import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;

import jakarta.inject.Inject;

public abstract class AbstractProcessorRouteFactoryBean<T> extends AbstractComponentFactory<T> {

  @Inject
  private ZernovaContext muleContext;

  @Inject
  protected ConfigurationComponentLocator locator;

  @Inject
  protected ComponentTracerFactory componentTracerFactory;

  private List<Processor> messageProcessors;

  public void setMessageProcessors(List<Processor> messageProcessors) {
    this.messageProcessors = messageProcessors;
  }

  @Override
  public T doGetObject() throws Exception {
    final DefaultMessageProcessorChainBuilder builder = new DefaultMessageProcessorChainBuilder();
    builder.chain(messageProcessors);
    MessageProcessorChain chain = newLazyProcessorChainBuilder(builder, muleContext,
                                                               () -> getProcessingStrategy(locator, this).orElse(null));
    return getProcessorRoute(chain);
  }

  protected abstract T getProcessorRoute(MessageProcessorChain chain);

}
