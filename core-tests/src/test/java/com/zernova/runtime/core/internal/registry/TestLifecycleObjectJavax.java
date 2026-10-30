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
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.core.api.ZernovaContext;

import javax.inject.Inject;

public class TestLifecycleObjectJavax implements Lifecycle {

  private int initialise = 0;
  private int start = 0;
  private int stop = 0;
  private int dispose = 0;

  @Inject
  private ObjectStoreManager objectStoreManager;

  @Inject
  private ZernovaContext muleContext;

  @Override
  public void initialise() throws InitialisationException {
    initialise++;
  }

  @Override
  public void start() throws ZernovaException {
    start++;
  }

  @Override
  public void stop() throws ZernovaException {
    stop++;
  }

  @Override
  public void dispose() {
    dispose++;
  }

  public int getInitialise() {
    return initialise;
  }

  public int getStart() {
    return start;
  }

  public int getStop() {
    return stop;
  }

  public int getDispose() {
    return dispose;
  }

  public ObjectStoreManager getObjectStoreManager() {
    return objectStoreManager;
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }
}
