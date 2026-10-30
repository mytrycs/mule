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
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.Config;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.lifecycle.LifecycleUtils;
import com.zernova.runtime.core.api.util.queue.QueueManager;
import com.zernova.runtime.core.internal.exception.GlobalErrorHandler;
import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The Stop phase for the Management context LifecycleManager. Calling {@link ZernovaContext#stop()} with initiate this phase via the
 * {@link com.zernova.runtime.core.api.lifecycle.LifecycleManager}.
 *
 *
 * The ZernovaContextDisposePhase defines the lifecycle behaviour when the Mule context is stopped. The ZernovaContext is associated
 * with one or more registries that inherit the lifecycle of the ZernovaContext.
 *
 * This phase is responsible for disposing objects. Any object that implements {@link Stoppable} will have its
 * {@link Stoppable#stop()} ()} method called. Objects are initialised in the order based on type:
 * {@link com.zernova.runtime.core.api.construct.FlowConstruct} followed by any other object that implements {@link Stoppable}.
 *
 * @see com.zernova.runtime.core.api.ZernovaContext
 * @see com.zernova.runtime.core.api.lifecycle.LifecycleManager
 * @see Stoppable
 *
 * @since 3.0
 */
public class ZernovaContextStopPhase extends DefaultLifecyclePhase {

  public ZernovaContextStopPhase() {
    this(new Class[] {
        Registry.class,
        ZernovaContext.class,
        Service.class
    });
  }

  public ZernovaContextStopPhase(Class<?>[] ignoredObjects) {
    super(Stoppable.PHASE_NAME, LifecycleUtils::stopIfNeeded);

    setOrderedLifecycleTypes(new Class<?>[] {
        FlowConstruct.class,
        ConfigurationProvider.class,
        Config.class,
        QueueManager.class,
        Stoppable.class
    });

    ignoredObjects = ArrayUtils.add(ignoredObjects, GlobalErrorHandler.class);

    setIgnoredObjectTypes(ignoredObjects);
    // Yuo can initialise and stop
    registerSupportedPhase(Initialisable.PHASE_NAME);
    // Stop/Start/Stop
    registerSupportedPhase(Startable.PHASE_NAME);
  }
}
