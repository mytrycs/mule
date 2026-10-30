/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle.phases;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.Config;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

/**
 * Objects are disposed of via the Registry since the Registry manages the creation/initialisation of the objects it must also
 * take care of disposing them. However, a user may want to initiate a dispose via the {@link DefaultZernovaContext} so the dispose
 * Lifecycle phase for the {@link DefaultZernovaContext} needs to call dispose on the Registry.
 *
 * The ZernovaContextDisposePhase defines the lifecycle behaviour when the Mule context is disposed. The ZernovaContext is associated
 * with one or more registries that inherit the lifecycle of the ZernovaContext.
 *
 * This phase is responsible for disposing objects. Any object that implements {@link Disposable} will have its
 * {@link Disposable#dispose()} method called.
 *
 * @see com.zernova.runtime.core.api.ZernovaContext
 * @see com.zernova.runtime.core.api.lifecycle.LifecycleManager
 * @see Disposable
 *
 * @since 3.0
 */
public class ZernovaContextDisposePhase extends DefaultLifecyclePhase {

  public ZernovaContextDisposePhase() {
    super(Disposable.PHASE_NAME, o -> {
      if (o instanceof Disposable) {
        ((Disposable) o).dispose();
      }
    });

    setOrderedLifecycleTypes(new Class<?>[] {
        FlowConstruct.class,
        ConfigurationProvider.class,
        Config.class,
        Disposable.class,
        Object.class
    });

    // Can call dispose from all lifecycle Phases
    registerSupportedPhase(LifecyclePhase.ALL_PHASES);
    /*
     * Ignored objects:
     *
     * * Component is ignored because the FlowConstruct will manage the components lifecycle
     *
     * * RouterCollection is ignored because the FlowConstruct will manage the lifecycle
     *
     * * Router is ignored since its lifecycle is managed by the associated router collection
     *
     * * Transformer is ignored since the Dispose lifecycle is managed by the base {@link AbstractTransformer} by receiving a
     * ARTIFACT_DISPOSING event and calling dispose on the transformer. This is necessary since transformers are prototype objects
     * and not managed by DI containers such as Spring after the creation of the object
     */
    setIgnoredObjectTypes(new Class[] {
        Transformer.class,
        ZernovaContext.class
    });
  }

  @Override
  public void applyLifecycle(Object o) throws LifecycleException {
    if (o == null) {
      return;
    }
    if (ignoreType(o.getClass())) {
      return;
    }
    // retain default Lifecycle behaviour
    try {
      super.applyLifecycle(o);
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to dispose object " + o, e);
      }
    }
  }
}
