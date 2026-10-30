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
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.api.config.i18n.CoreMessages;
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;


/**
 * <p>
 * Default implementation of a {@link SimpleLifecycleManager} it allows {@link Lifecycle} objects to manage their lifecycle
 * easily.
 * </p>
 *
 * @param <T> Type of the object that we need to manage the lifecycle
 * @since 3.5.0
 */
public class DefaultLifecycleManager<T extends Lifecycle> extends SimpleLifecycleManager<T> {


  public DefaultLifecycleManager(String id, T object) {
    super(id, object);
  }


  @Override
  public synchronized void fireInitialisePhase(LifecycleCallback<T> callback) throws InitialisationException {
    checkPhase(Initialisable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Initialising Bean: " + lifecycleManagerId);
    }
    try {
      invokePhase(Initialisable.PHASE_NAME, getLifecycleObject(), callback);
    } catch (InitialisationException e) {
      throw e;
    } catch (LifecycleException e) {
      throw new InitialisationException(e, object);
    }
  }

  @Override
  public synchronized void fireStartPhase(LifecycleCallback<T> callback) throws ZernovaException {
    checkPhase(Startable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Starting Bean: " + lifecycleManagerId);
    }
    invokePhase(Startable.PHASE_NAME, getLifecycleObject(), callback);
  }

  @Override
  public synchronized void fireStopPhase(LifecycleCallback<T> callback) throws ZernovaException {
    checkPhase(Stoppable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Stopping Bean: " + lifecycleManagerId);
    }
    invokePhase(Stoppable.PHASE_NAME, getLifecycleObject(), callback);
  }

  @Override
  public synchronized void fireDisposePhase(LifecycleCallback<T> callback) {
    checkPhase(Disposable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Disposing Bean: " + lifecycleManagerId);
    }
    try {
      invokePhase(Disposable.PHASE_NAME, getLifecycleObject(), callback);
    } catch (LifecycleException e) {
      logger.warn(CoreMessages.failedToDispose(lifecycleManagerId).toString(), e);
    }
  }

}
