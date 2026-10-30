/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle.phases;

import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.ioc.ObjectProvider;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.Config;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.lifecycle.LifecycleUtils;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

/**
 * The ZernovaContextInitialisePhase defines the lifecycle behaviour when the Mule context is initialised. The ZernovaContext is
 * associated with one or more registries that inherit the lifecycle of the ZernovaContext.
 * <p/>
 * This phase is responsible for initialising objects. Any object that implements {@link Initialisable} will have its
 * {@link Initialisable#initialise()} method called. Objects are initialised in the order based on type:
 * {@link com.zernova.runtime.core.api.construct.FlowConstruct}, followed by any other object that implements {@link Initialisable}.
 *
 * @see com.zernova.runtime.core.api.ZernovaContext
 * @see com.zernova.runtime.core.api.lifecycle.LifecycleManager
 * @see Initialisable
 * @since 3.0
 */
public class ZernovaContextInitialisePhase extends DefaultLifecyclePhase {

  public ZernovaContextInitialisePhase() {
    super(Initialisable.PHASE_NAME, LifecycleUtils::initialiseIfNeeded);
    registerSupportedPhase(NotInLifecyclePhase.PHASE_NAME);
    setOrderedLifecycleTypes(new Class<?>[] {
        StreamingManager.class,
        ObjectStore.class,
        ExpressionLanguage.class,
        ConfigurationProvider.class,
        Config.class,
        SecurityManager.class,
        FlowConstruct.class,
        Initialisable.class
    });
    setIgnoredObjectTypes(new Class[] {
        ZernovaContext.class,
        ObjectProvider.class
    });
  }

  @Override
  public void applyLifecycle(Object o) throws LifecycleException {
    // retain default Lifecycle behaviour
    super.applyLifecycle(o);
    if (o == null) {
      return;
    }
    if (ignoreType(o.getClass())) {
      return;
    }
  }
}
