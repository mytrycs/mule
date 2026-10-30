/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle.phases;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.Config;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.api.lifecycle.LifecycleUtils;
import com.zernova.runtime.core.api.util.queue.QueueManager;
import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

/**
 * The Start phase for the ZernovaContext. Calling {@link ZernovaContext#start()} will initiate this phase via the
 * {@link com.zernova.runtime.core.api.lifecycle.LifecycleManager}.
 * <p/>
 * The ZernovaContextStartPhase defines the lifecycle behaviour when the Mule context is started. The ZernovaContext is associated with
 * one or more registries that inherit the lifecycle of the ZernovaContext.
 * <p/>
 * This phase is responsible for starting objects. Any object that implements {@link Startable} will have its
 * {@link Startable#start()} method called. Objects are initialised in the order based on type:
 * {@link com.zernova.runtime.core.api.construct.FlowConstruct}, followed by any other object that implements {@link Startable}.
 *
 * @see com.zernova.runtime.core.api.ZernovaContext
 * @see com.zernova.runtime.core.api.lifecycle.LifecycleManager
 * @see Startable
 * @since 3.0
 */
public class ZernovaContextStartPhase extends DefaultLifecyclePhase {

  public ZernovaContextStartPhase() {
    this(new Class[] {
        Registry.class,
        ZernovaContext.class,
        Service.class,
        FlowExceptionHandler.class
    });
  }

  public ZernovaContextStartPhase(Class<?>[] ignoredObjects) {
    super(Startable.PHASE_NAME, LifecycleUtils::startIfNeeded);

    setIgnoredObjectTypes(ignoredObjects);
    setOrderedLifecycleTypes(new Class<?>[] {
        QueueManager.class,
        ObjectStore.class,
        ConfigurationProvider.class,
        Config.class,
        FlowConstruct.class,
        Startable.class
    });

    registerSupportedPhase(Initialisable.PHASE_NAME);
    // Start/Stop/Start
    registerSupportedPhase(Stoppable.PHASE_NAME);
  }
}
