/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.core.api.ZernovaContext;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

public class LifecycleObject extends AbstractComponent implements Lifecycle {

  @Inject
  private ZernovaContext muleContext;

  private LifecycleObject otherLifecycleObject;

  private String failurePhase;
  private List<String> lifecycleInvocations = new ArrayList<>();

  public void setOtherLifecycleObject(LifecycleObject otherLifecycleObject) {
    this.otherLifecycleObject = otherLifecycleObject;
  }

  public void setFailurePhase(String failurePhase) {
    this.failurePhase = failurePhase;
  }

  public List<String> getLifecycleInvocations() {
    return lifecycleInvocations;
  }

  private void failIfNeeded(String phase) {
    if (failurePhase != null && failurePhase.equalsIgnoreCase(phase)) {
      throw new RuntimeException("generated failure");
    }
  }

  @Override
  public void stop() throws ZernovaException {
    lifecycleInvocations.add("stop");
    failIfNeeded("stop");
  }

  @Override
  public void dispose() {
    lifecycleInvocations.add("dispose");
    failIfNeeded("dispose");
  }

  @Override
  public void start() throws ZernovaException {
    lifecycleInvocations.add("start");
    failIfNeeded("start");
  }

  @Override
  public void initialise() throws InitialisationException {
    lifecycleInvocations.add("initialise");
    failIfNeeded("initialise");
  }

  public LifecycleObject getOtherLifecycleObject() {
    return otherLifecycleObject;
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }
}
