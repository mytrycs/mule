/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

public abstract class AbstractLifecycleTracker implements Lifecycle, ZernovaContextAware {

  private final List<String> tracker = new ArrayList<>();

  public List<String> getTracker() {
    return tracker;
  }

  public void setProperty(final String value) {
    tracker.add("setProperty");
  }

  @Inject
  public void setMuleContext(final ZernovaContext context) {
    tracker.add("setMuleContext");
  }

  @Override
  public void initialise() throws InitialisationException {
    tracker.add("initialise");
  }

  @Override
  public void start() throws ZernovaException {
    tracker.add("start");
  }

  @Override
  public void stop() throws ZernovaException {
    tracker.add("stop");
  }

  @Override
  public void dispose() {
    tracker.add("dispose");
  }

}
