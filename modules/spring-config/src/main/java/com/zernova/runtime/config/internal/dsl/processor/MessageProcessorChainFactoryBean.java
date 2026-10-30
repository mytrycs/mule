/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.processor;

import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;
import static com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder.newLazyProcessorChainBuilder;

import static java.util.Collections.emptyMap;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.chain.AbstractMessageProcessorChainBuilder;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import javax.xml.namespace.QName;

@NoExtend
public class MessageProcessorChainFactoryBean extends AbstractComponentFactory<MessageProcessorChain>
    implements ZernovaContextAware {

  protected List<Processor> processors;
  protected String name;
  protected ZernovaContext muleContext;

  @Inject
  protected ConfigurationComponentLocator locator;

  public void setMessageProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  public Map<QName, Object> getAnnotations() {
    return emptyMap();
  }

  @Override
  public MessageProcessorChain doGetObject() throws Exception {
    AbstractMessageProcessorChainBuilder builder = getBuilderInstance();
    builder.chain(processors.toArray(Processor[]::new));
    return newLazyProcessorChainBuilder(builder,
                                        muleContext,
                                        () -> getProcessingStrategy(locator, this).orElse(null));
  }

  protected AbstractMessageProcessorChainBuilder getBuilderInstance() {
    return new DefaultMessageProcessorChainBuilder();
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

}
