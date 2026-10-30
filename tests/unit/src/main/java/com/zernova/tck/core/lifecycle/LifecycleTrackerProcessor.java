/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.core.lifecycle;

import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.management.stats.ComponentStatistics;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.tck.core.lifecyle.AbstractLifecycleTracker;

import jakarta.inject.Inject;

public class LifecycleTrackerProcessor extends AbstractLifecycleTracker implements Processor {

  public static String LIFECYCLE_TRACKER_PROCESSOR_PROPERTY = "lifecycle";
  public static String FLOW_CONSRUCT_PROPERTY = "flowConstruct";

  @Inject
  private Registry registry;

  public void springInitialize() {
    getTracker().add("springInitialize");
  }

  public void springDestroy() {
    getTracker().add("springDestroy");
  }

  public ComponentStatistics getStatistics() {
    return mock(ComponentStatistics.class);
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    event = CoreEvent.builder(event)
        .addVariable(LIFECYCLE_TRACKER_PROCESSOR_PROPERTY, getTracker().toString())
        .addVariable(FLOW_CONSRUCT_PROPERTY, registry.lookupByName(getRootContainerLocation().toString()).orElse(null)).build();
    return event;
  }
}
