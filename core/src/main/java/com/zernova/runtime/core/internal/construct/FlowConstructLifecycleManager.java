/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.construct;

import static com.zernova.runtime.api.notification.FlowConstructNotification.FLOW_CONSTRUCT_DISPOSED;
import static com.zernova.runtime.api.notification.FlowConstructNotification.FLOW_CONSTRUCT_INITIALISED;
import static com.zernova.runtime.api.notification.FlowConstructNotification.FLOW_CONSTRUCT_STARTED;
import static com.zernova.runtime.api.notification.FlowConstructNotification.FLOW_CONSTRUCT_STOPPED;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.notification.FlowConstructNotification;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;
import com.zernova.runtime.core.internal.lifecycle.SimpleLifecycleManager;

import org.slf4j.Logger;

/**
 * The lifecycle manager responsible for managing lifecycle transitions for a Mule service. The Mule service adds some additional
 * states, namely pause and resume. The lifecycle manager manages lifecycle notifications and logging as well.
 */
public class FlowConstructLifecycleManager extends SimpleLifecycleManager<FlowConstruct> {

  /**
   * logger used by this class
   */
  private static final Logger logger = getLogger(FlowConstructLifecycleManager.class);
  private final NotificationDispatcher notificationFirer;

  public FlowConstructLifecycleManager(FlowConstruct flowConstruct, NotificationDispatcher notificationFirer) {
    super(flowConstruct.getName(), flowConstruct);
    this.notificationFirer = notificationFirer;
  }

  @Override
  public synchronized void fireInitialisePhase(LifecycleCallback<FlowConstruct> callback) throws ZernovaException {
    checkPhase(Initialisable.PHASE_NAME);
    // TODO No pre notification
    if (logger.isInfoEnabled()) {
      logger.info("Initialising flow: " + getLifecycleObject().getName());
    }
    invokePhase(Initialisable.PHASE_NAME, getLifecycleObject(), callback);
    fireNotification(FLOW_CONSTRUCT_INITIALISED);
  }


  @Override
  public synchronized void fireStartPhase(LifecycleCallback<FlowConstruct> callback) throws ZernovaException {
    checkPhase(Startable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Starting flow: " + getLifecycleObject().getName());
    }
    // TODO No pre notification
    invokePhase(Startable.PHASE_NAME, getLifecycleObject(), callback);
    fireNotification(FLOW_CONSTRUCT_STARTED);
  }


  @Override
  public synchronized void fireStopPhase(LifecycleCallback<FlowConstruct> callback) throws ZernovaException {
    checkPhase(Stoppable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Stopping flow: " + getLifecycleObject().getName());
    }
    // TODO No pre notification
    invokePhase(Stoppable.PHASE_NAME, getLifecycleObject(), callback);
    fireNotification(FLOW_CONSTRUCT_STOPPED);
  }

  @Override
  public synchronized void fireDisposePhase(LifecycleCallback<FlowConstruct> callback) throws ZernovaException {
    checkPhase(Disposable.PHASE_NAME);
    if (logger.isInfoEnabled()) {
      logger.info("Disposing flow: " + getLifecycleObject().getName());
    }
    // TODO No pre notification
    invokePhase(Disposable.PHASE_NAME, getLifecycleObject(), callback);
    fireNotification(FLOW_CONSTRUCT_DISPOSED);
  }

  protected void fireNotification(int action) {
    notificationFirer.dispatch(new FlowConstructNotification(getLifecycleObject().getName(), action));
  }
}
