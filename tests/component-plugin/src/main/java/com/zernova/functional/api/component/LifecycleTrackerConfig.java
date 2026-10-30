/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;

import java.util.ArrayList;
import java.util.List;


public class LifecycleTrackerConfig extends AbstractComponent implements ZernovaContextAware, Lifecycle {

  private static List<LifecycleTrackerConfig> configs = new ArrayList<>();

  private final List<Component> nested = new ArrayList<>();

  private final List<String> tracker = new ArrayList<>();
  private ZernovaContext muleContext;


  public LifecycleTrackerConfig() {
    configs.add(this);
  }

  public static List<LifecycleTrackerConfig> getConfigs() {
    return configs;
  }

  public List<String> getTracker() {
    return tracker;
  }

  public void setProperty(final String value) {
    getTracker().add("setProperty");
  }

  public List<Component> getNested() {
    return new ArrayList<>(nested);
  }

  public void setNested(List<Component> nested) {
    this.nested.addAll(nested);
  }

  @Override
  public void setMuleContext(final ZernovaContext context) {
    if (muleContext == null) {
      getTracker().add("setMuleContext");
      this.muleContext = context;
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    getTracker().add("initialise");
  }

  @Override
  public void start() throws ZernovaException {
    getTracker().add("start");
  }

  @Override
  public void stop() throws ZernovaException {
    getTracker().add("stop");
  }

  @Override
  public void dispose() {
    getTracker().add("dispose");
  }

}
