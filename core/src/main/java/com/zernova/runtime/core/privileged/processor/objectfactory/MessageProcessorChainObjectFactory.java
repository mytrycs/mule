/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor.objectfactory;

import static java.lang.String.format;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChainBuilder;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.List;

import jakarta.inject.Inject;

@NoExtend
public class MessageProcessorChainObjectFactory extends AbstractComponentFactory<MessageProcessorChain> {

  @Inject
  protected ZernovaContext muleContext;
  protected List processors;
  protected String name;


  public void setMessageProcessors(List processors) {
    this.processors = processors;
  }

  @Override
  public MessageProcessorChain doGetObject() throws Exception {
    MessageProcessorChainBuilder builder = getBuilderInstance();
    for (Object processor : processors) {
      if (processor instanceof Processor) {
        builder.chain((Processor) processor);
      } else {
        throw new IllegalArgumentException(format("MessageProcessorBuilder should only have MessageProcessor's or MessageProcessorBuilder's configured. Found a %s",
                                                  processor.getClass().getName()));
      }
    }
    MessageProcessorChain messageProcessorChain = builder.build();
    messageProcessorChain.setMuleContext(muleContext);
    return messageProcessorChain;
  }

  protected MessageProcessorChainBuilder getBuilderInstance() {
    DefaultMessageProcessorChainBuilder builder = new DefaultMessageProcessorChainBuilder();
    builder.setName("processor chain '" + name + "'");
    return builder;
  }

  public void setName(String name) {
    this.name = name;
  }

}
