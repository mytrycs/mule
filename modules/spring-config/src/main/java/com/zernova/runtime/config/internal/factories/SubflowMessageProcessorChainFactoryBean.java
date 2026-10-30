/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static java.lang.String.format;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.processor.chain.SubflowMessageProcessorChainBuilder;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;

import jakarta.inject.Inject;

/**
 * Uses a custom message processor chain builder for subflows in order to generate the proper message processor ids.
 */
public class SubflowMessageProcessorChainFactoryBean extends AbstractComponentFactory<SubflowMessageProcessorChainBuilder> {

  @Inject
  protected ZernovaContext muleContext;

  @Inject
  ComponentTracerFactory componentTracerFactory;

  protected List processors;
  protected String name;

  public void setMessageProcessors(List processors) {
    this.processors = processors;
  }

  @Override
  public SubflowMessageProcessorChainBuilder doGetObject() throws Exception {
    SubflowMessageProcessorChainBuilder builder = getBuilderInstance();
    for (Object processor : processors) {
      if (processor instanceof Processor) {
        builder.chain((Processor) processor);
      } else {
        throw new IllegalArgumentException(format("MessageProcessorBuilder should only have MessageProcessor's or MessageProcessorBuilder's configured. Found a %s",
                                                  processor.getClass().getName()));
      }
    }
    return builder;
  }

  protected SubflowMessageProcessorChainBuilder getBuilderInstance() {
    SubflowMessageProcessorChainBuilder builder = new SubflowMessageProcessorChainBuilder();
    builder.withComponentTracerFactory(componentTracerFactory);
    builder.setAnnotations(getAnnotations());
    builder.setName(name);
    return builder;
  }

  public void setName(String name) {
    this.name = name;
  }
}
