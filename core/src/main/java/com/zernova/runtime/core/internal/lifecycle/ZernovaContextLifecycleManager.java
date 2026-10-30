/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.connector.ConnectException;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.lifecycle.phases.NotInLifecyclePhase;

/**
 * This is a specialized class that extends {@link RegistryLifecycleManager} and will invoke lifecycle on the registry instance
 * for the ZernovaContext. This class must only be used by the ZernovaContext.
 */
public class ZernovaContextLifecycleManager extends AbstractLifecycleManager<ZernovaContext> implements ZernovaContextAware {

  private ZernovaContext muleContext;

  private ZernovaContextLifecycleCallback callback = new ZernovaContextLifecycleCallback();

  public ZernovaContextLifecycleManager() {
    // We cannot pass in a ZernovaContext on creation since the context is not actually created when this object is needed
    super("ZernovaContext", null);
  }

  @Override
  protected void registerTransitions() {
    // init dispose
    addDirectTransition(NotInLifecyclePhase.PHASE_NAME, Initialisable.PHASE_NAME);
    addDirectTransition(NotInLifecyclePhase.PHASE_NAME, Disposable.PHASE_NAME);
    addDirectTransition(Initialisable.PHASE_NAME, Startable.PHASE_NAME);
    addDirectTransition(Initialisable.PHASE_NAME, Disposable.PHASE_NAME);

    // start stop
    addDirectTransition(Startable.PHASE_NAME, Stoppable.PHASE_NAME);
    addDirectTransition(Stoppable.PHASE_NAME, Startable.PHASE_NAME);
    addDirectTransition(Stoppable.PHASE_NAME, Disposable.PHASE_NAME);
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
    this.object = muleContext;
  }

  @Override
  public void fireLifecycle(String destinationPhase) throws LifecycleException {
    checkPhase(destinationPhase);
    invokePhase(destinationPhase, object, callback);
  }

  @Override
  protected void doOnConnectException(ConnectException ce) throws LifecycleException {
    throw new LifecycleException(ce, this);
  }

  class ZernovaContextLifecycleCallback implements LifecycleCallback<ZernovaContext> {

    @Override
    public void onTransition(String phaseName, ZernovaContext muleContext) throws ZernovaException {
      ((ZernovaContextWithRegistry) muleContext).getRegistry().fireLifecycle(phaseName);
    }
  }
}
