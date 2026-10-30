/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test message processor to keep count of number of invocations.
 */
public class InvocationCountMessageProcessor extends AbstractComponent implements Processor, Initialisable, Startable {

  private static Map<String, AtomicInteger> invocationCountPerMessageProcessor = new HashMap<>();
  private AtomicInteger invocationCount;
  private String name;


  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    invocationCount.incrementAndGet();
    return event;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void initialise() throws InitialisationException {
    this.invocationCount = invocationCountPerMessageProcessor.computeIfAbsent(this.name, k -> new AtomicInteger(0));
  }

  @Override
  public void start() throws ZernovaException {
    this.invocationCount.set(0);
  }

  /**
   * @param componentName name of the message processor in the configuration
   * @return the number of invocations for the message processor with name componentName
   */
  public static int getNumberOfInvocationsFor(String componentName) {
    AtomicInteger count = invocationCountPerMessageProcessor.get(componentName);
    if (count == null) {
      throw new IllegalArgumentException("No invocation-counter component registered under name: " + componentName
          + ", registered components: " + invocationCountPerMessageProcessor.keySet());
    }
    return count.get();
  }

}
