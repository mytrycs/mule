/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.util.collection.SmallMap;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.connector.ConnectException;
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;
import com.zernova.runtime.core.internal.lifecycle.phases.LifecycleObjectSorter;
import com.zernova.runtime.core.internal.lifecycle.phases.LifecyclePhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextDisposePhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextInitialisePhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextStartPhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextStopPhase;
import com.zernova.runtime.core.internal.lifecycle.phases.NotInLifecyclePhase;
import com.zernova.runtime.core.internal.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;


public class RegistryLifecycleManager extends AbstractLifecycleManager<Registry> {


  private final Map<String, LifecyclePhase> phases = new SmallMap<>();
  private final SortedMap<String, LifecycleCallback> callbacks = new TreeMap<>();

  private final ZernovaContext muleContext;
  private final LifecycleInterceptor lifecycleInterceptor;

  public RegistryLifecycleManager(String id, Registry object, ZernovaContext muleContext,
                                  LifecycleInterceptor lifecycleInterceptor) {
    super(id, object);
    this.muleContext = muleContext;
    this.lifecycleInterceptor = requireNonNull(lifecycleInterceptor);

    registerPhases(object);
  }

  protected void registerPhases(Registry registry) {
    final RegistryLifecycleCallback<Object> callback = new RegistryLifecycleCallback<>(this);

    registerPhase(NotInLifecyclePhase.PHASE_NAME, new NotInLifecyclePhase(), new EmptyLifecycleCallback<>());
    registerPhase(Initialisable.PHASE_NAME, new ZernovaContextInitialisePhase(), callback);
    registerPhase(Startable.PHASE_NAME, new ZernovaContextStartPhase(), callback);
    registerPhase(Stoppable.PHASE_NAME, new ZernovaContextStopPhase(), callback);
    registerPhase(Disposable.PHASE_NAME, new ZernovaContextDisposePhase(), callback);
  }

  @Override
  protected void registerTransitions() {
    addDirectTransition(NotInLifecyclePhase.PHASE_NAME, Initialisable.PHASE_NAME);
    addDirectTransition(Initialisable.PHASE_NAME, Startable.PHASE_NAME);

    // start stop
    addDirectTransition(Startable.PHASE_NAME, Stoppable.PHASE_NAME);
    addDirectTransition(Stoppable.PHASE_NAME, Startable.PHASE_NAME);
    // Dispose can be called from init or stopped
    addDirectTransition(NotInLifecyclePhase.PHASE_NAME, Disposable.PHASE_NAME);
    addDirectTransition(Initialisable.PHASE_NAME, Disposable.PHASE_NAME);
    addDirectTransition(Stoppable.PHASE_NAME, Disposable.PHASE_NAME);

  }

  protected void registerPhase(String phaseName, LifecyclePhase phase) {
    registerPhase(phaseName, phase, new RegistryLifecycleCallback(this));
  }

  protected void registerPhase(String phaseName, LifecyclePhase phase, LifecycleCallback callback) {
    if (callback instanceof HasLifecycleInterceptor) {
      if (Initialisable.PHASE_NAME.equals(phaseName) || Disposable.PHASE_NAME.equals(phaseName)) {
        ((HasLifecycleInterceptor) callback).setLifecycleInterceptor(lifecycleInterceptor);
      }
      if (Startable.PHASE_NAME.equals(phaseName) || Stoppable.PHASE_NAME.equals(phaseName)) {
        ((HasLifecycleInterceptor) callback).setLifecycleInterceptor(lifecycleInterceptor);
      }
    }

    phaseNames.add(phaseName);
    callbacks.put(phaseName, callback);
    phases.put(phaseName, phase);
  }

  @Override
  public synchronized void fireLifecycle(String destinationPhase) throws LifecycleException {
    checkPhase(destinationPhase);
    if (isDirectTransition(destinationPhase) || isLastPhaseExecutionFailed()) {
      // transition to phase without going through other phases first
      invokePhase(destinationPhase, object, callbacks.get(destinationPhase));
    } else {
      // Call all phases to including the destination phase
      boolean start = false;
      for (String phase : phaseNames) {
        if (start) {
          invokePhase(phase, object, callbacks.get(phase));
          if (phase.equals(destinationPhase)) {
            break;
          }
        }
        if (phase.equals(getCurrentPhase())) {
          start = true;
        }
      }
    }
  }

  @Override
  protected void doOnConnectException(ConnectException ce) throws LifecycleException {
    throw new LifecycleException(ce, this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applyPhase(Object object, String startPhase, String toPhase) throws LifecycleException {
    // TODO i18n
    if (startPhase == null || toPhase == null) {
      throw new IllegalArgumentException("toPhase and fromPhase must be null");
    }
    if (!phaseNames.contains(startPhase)) {
      throw new IllegalArgumentException("fromPhase '" + startPhase + "' not a valid phase.");
    }
    if (!phaseNames.contains(toPhase)) {
      throw new IllegalArgumentException("toPhase '" + startPhase + "' not a valid phase.");
    }

    boolean started = false;
    for (String phaseName : phaseNames) {
      if (started) {
        doApplyLifecycle(object, phaseName);
      }
      if (toPhase.equals(phaseName)) {
        break;
      }
      if (phaseName.equals(startPhase)) {
        started = true;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applyCompletedPhases(Object object) throws LifecycleException {
    String lastPhase = NotInLifecyclePhase.PHASE_NAME;
    for (String phase : completedPhases) {
      if (isDirectTransition(lastPhase, phase)) {
        doApplyLifecycle(object, phase);
        lastPhase = phase;
      }
    }
  }

  private void doApplyLifecycle(Object object, String phase) throws LifecycleException {
    LifecyclePhase lp = phases.get(phase);
    lifecycleInterceptor.beforePhaseExecution(lp, object);
    try {
      lp.applyLifecycle(object);
      lifecycleInterceptor.afterPhaseExecution(lp, object, empty());
    } catch (Exception e) {
      lifecycleInterceptor.afterPhaseExecution(lp, object, of(e));
      throw e;
    }
  }

  protected List<Object> getObjectsForPhase(LifecyclePhase phase) {
    LifecycleObjectSorter sorter = phase.newLifecycleObjectSorter();
    Map<String, Object> lookupObjectsForLifecycle = lookupObjectsForLifecycle();
    List<String> lookupObjects = new ArrayList<>();

    lookupObjectsForLifecycle.forEach((key, value) -> {
      sorter.addObject(key, value);
      lookupObjects.add(key);
    });
    sorter.setLifeCycleObjectNameOrder(lookupObjects);
    return sorter.getSortedObjects();
  }

  protected Map<String, Object> lookupObjectsForLifecycle() {
    return getLifecycleObject().lookupByType(Object.class);
  }

  protected Optional<ZernovaContext> getMuleContext() {
    return ofNullable(muleContext);
  }

  protected LifecyclePhase getPhase(String phaseName) {
    return phases.get(phaseName);
  }
}
