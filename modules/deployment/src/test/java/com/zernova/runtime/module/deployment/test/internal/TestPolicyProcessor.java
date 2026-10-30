/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Component used on deployment test that require policies to check that they are invoked
 * <p/>
 * Static state must be reset before each test is executed
 */
public class TestPolicyProcessor extends AbstractComponent implements com.zernova.runtime.core.api.processor.Processor {

  public static volatile int invocationCount;
  public static volatile Map<String, AtomicInteger> correlationIdCount = new ConcurrentHashMap<>();
  public static volatile String policyParametrization = "";

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    invocationCount++;
    String variableName = "policyParameter";
    if (event.getVariables().keySet().contains(variableName)) {
      policyParametrization += event.getVariables().get(variableName).getValue();
    }

    correlationIdCount.computeIfAbsent(event.getCorrelationId(), k -> new AtomicInteger(0)).addAndGet(1);

    return event;
  }
}
