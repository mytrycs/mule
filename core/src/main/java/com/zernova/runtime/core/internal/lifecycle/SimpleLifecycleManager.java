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
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;
import com.zernova.runtime.core.api.lifecycle.LifecycleManager;
import com.zernova.runtime.core.internal.lifecycle.phases.NotInLifecyclePhase;

/**
 * This {@link LifecycleManager} implementation is designed to track the lifecycle of objects that implement any of the
 * {@link Initialisable}, {@link Startable}, {@link Stoppable} or {@link Disposable} interfaces.
 * <p>
 * Also, adds convenience methods for firing these phases by callbacks.
 *
 * @param <O> the object type being managed by this {@link LifecycleManager}
 */
public abstract class SimpleLifecycleManager<O> extends AbstractLifecycleManager<O> {

  public SimpleLifecycleManager(String id, O object) {
    super(id, object);
  }

  @Override
  protected void registerTransitions() {
    // init dispose
    addDirectTransition(NotInLifecyclePhase.PHASE_NAME, Initialisable.PHASE_NAME);
    addDirectTransition(NotInLifecyclePhase.PHASE_NAME, Disposable.PHASE_NAME);
    addDirectTransition(Initialisable.PHASE_NAME, Startable.PHASE_NAME);

    // If an object fails to start, the object can be left in an initialise state, but the container can be started
    addDirectTransition(Initialisable.PHASE_NAME, Stoppable.PHASE_NAME);
    addDirectTransition(Initialisable.PHASE_NAME, Disposable.PHASE_NAME);

    // start stop
    addDirectTransition(Startable.PHASE_NAME, Stoppable.PHASE_NAME);
    addDirectTransition(Stoppable.PHASE_NAME, Startable.PHASE_NAME);
    addDirectTransition(Stoppable.PHASE_NAME, Disposable.PHASE_NAME);
  }

  public void fireLifecycle(String phase) throws LifecycleException {
    throw new UnsupportedOperationException("SimpleLifecycleManager.fireLifecycle");
  }

  public abstract void fireInitialisePhase(LifecycleCallback<O> callback) throws ZernovaException;

  public abstract void fireStartPhase(LifecycleCallback<O> callback) throws ZernovaException;

  public abstract void fireStopPhase(LifecycleCallback<O> callback) throws ZernovaException;

  public abstract void fireDisposePhase(LifecycleCallback<O> callback) throws ZernovaException;
}
