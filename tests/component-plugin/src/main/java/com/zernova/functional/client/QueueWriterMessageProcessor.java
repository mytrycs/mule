/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.client;

import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.el.BindingContextUtils.addEventBindings;
import static com.zernova.runtime.api.metadata.DataType.fromType;

import com.zernova.functional.api.component.TestConnectorQueueHandler;
import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;

import jakarta.inject.Inject;

/**
 * Writes {@link CoreEvent} to a test connector's queue.
 */
public class QueueWriterMessageProcessor extends AbstractComponent implements Processor, Initialisable, Startable, Stoppable {

  @Inject
  private ExtendedExpressionManager expressionManager;

  private volatile boolean started = false;

  private String name;
  private String content;
  private Class contentJavaType;
  private TestConnectorQueueHandler queueHandler;

  @Inject
  private Registry registry;

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    if (!this.started) {
      throw new IllegalStateException("`test:queue` component not started.");
    }

    String attribute = content == null ? "#[payload]" : content;

    final TypedValue evaluated =
        expressionManager.evaluate(attribute,
                                   fromType(contentJavaType == null ? Object.class : contentJavaType),
                                   addEventBindings(event, NULL_BINDING_CONTEXT));

    CoreEvent copy;
    Object payloadValue = evaluated.getValue();
    copy = CoreEvent.builder(event).message(Message.builder(event.getMessage()).value(payloadValue).build())
        // Queue works based on ZernovaEvent for testing purposes. A real operation
        // would not be aware of the error field and just the plain message would be sent.
        .error(null)
        .build();

    queueHandler.write(name, copy);

    return event;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setContentJavaType(Class contentJavaType) {
    this.contentJavaType = contentJavaType;
  }

  @Override
  public void initialise() throws InitialisationException {
    queueHandler = new TestConnectorQueueHandler(registry);
  }

  @Override
  public synchronized void start() throws ZernovaException {
    this.started = true;
  }

  @Override
  public synchronized void stop() throws ZernovaException {
    this.started = false;
  }

}
