/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.receiver;

import static java.lang.Thread.currentThread;
import static com.zernova.runtime.core.privileged.event.PrivilegedEvent.getCurrentEvent;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReceiver extends AbstractComponent implements Processor, ZernovaContextAware {

  protected static final Logger logger = LoggerFactory.getLogger(TestReceiver.class);

  private ZernovaContext muleContext;

  protected AtomicInteger count = new AtomicInteger(0);

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    try {
      final Message message = event.getMessage();

      return CoreEvent.builder(event)
          .message(Message.builder(message)
              .value(receive(muleContext.getTransformationService().transform(event.getMessage(), DataType.STRING).getPayload()
                  .getValue().toString()))
              .build())
          .build();
    } catch (Exception e) {
      throw new DefaultZernovaException(e);
    }
  }

  public String receive(String message) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("Received: " + message + " Number: " + inc() + " in thread: " + currentThread().getName());
      logger.debug("Message ID is: " + getCurrentEvent().getCorrelationId());
    }

    return "Received: " + message;
  }

  protected int inc() {
    return count.incrementAndGet();
  }

  @Override
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }
}
