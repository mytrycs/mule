/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.testmodels.fruit;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.lifecycle.InitialisationCallback;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.object.ObjectFactory;

/**
 * <code>BananaFactory</code> is a test factory that creates Bananas
 */
public class BananaFactory implements ObjectFactory {

  @Override
  public void initialise() throws InitialisationException {
    // nothing to do
  }

  @Override
  public void dispose() {
    // nothing to do
  }

  @Override
  public Object getInstance(ZernovaContext muleContext) throws Exception {
    return new Banana();
  }

  @Override
  public Class<?> getObjectClass() {
    return Banana.class;
  }

  @Override
  public void addObjectInitialisationCallback(InitialisationCallback callback) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  @Override
  public boolean isExternallyManagedLifecycle() {
    return false;
  }

  @Override
  public boolean isAutoWireObject() {
    return false;
  }
}
