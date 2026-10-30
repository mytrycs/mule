/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.privileged.registry.RegistrationException;

import java.util.Collection;
import java.util.Map;

/**
 * Adds lookup/register/unregister methods for Mule-specific entities to the standard Registry interface.
 */
public class ZernovaRegistryHelper implements ZernovaRegistry {

  /**
   * A reference to Mule's internal registry
   */
  private final Registry registry;

  private final ZernovaContext muleContext;

  public ZernovaRegistryHelper(Registry registry, ZernovaContext muleContext) {
    this.registry = registry;
    this.muleContext = muleContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialise() throws InitialisationException {
    // no-op

    // This is called when the ZernovaContext starts up, and should only do initialisation for any state on this class, the lifecycle
    // for the registries will be handled by the LifecycleManager on the registry that this class wraps
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    registry.dispose();
  }

  @Override
  public synchronized void fireLifecycle(String phase) throws LifecycleException {
    if (Initialisable.PHASE_NAME.equals(phase)) {
      registry.initialise();
    } else if (Disposable.PHASE_NAME.equals(phase)) {
      registry.dispose();
    } else {
      registry.fireLifecycle(phase);
    }
  }

  @Override
  public ZernovaContext getMuleContext() {
    return muleContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FlowConstruct lookupFlowConstruct(String name) {
    return (FlowConstruct) registry.lookupObject(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<FlowConstruct> lookupFlowConstructs() {
    return lookupObjects(FlowConstruct.class);
  }

  @Override
  public boolean isSingleton(String key) {
    return registry.isSingleton(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerFlowConstruct(FlowConstruct flowConstruct) throws ZernovaException {
    registry.registerObject(flowConstruct.getName(), flowConstruct);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object applyProcessorsAndLifecycle(Object object) throws ZernovaException {
    object = applyProcessors(object);
    object = applyLifecycle(object);
    return object;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object applyProcessors(Object object) throws ZernovaException {
    return muleContext.getInjector().inject(object);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object applyLifecycle(Object object) throws ZernovaException {
    return applyLifecycle(object, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object applyLifecycle(Object object, String phase) throws ZernovaException {
    return registry.applyLifecycle(object, phase);
  }

  @Override
  public void applyLifecycle(Object object, String startPhase, String toPhase) throws ZernovaException {
    registry.applyLifecycle(object, startPhase, toPhase);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T lookupObject(Class<T> type) throws RegistrationException {
    return registry.lookupObject(type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T lookupObject(String key) {
    return (T) registry.lookupObject(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T lookupObject(String key, boolean applyLifecycle) {
    return (T) registry.lookupObject(key, applyLifecycle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Collection<T> lookupObjects(Class<T> type) {
    return registry.lookupObjects(type);
  }

  @Override
  public <T> Collection<T> lookupLocalObjects(Class<T> type) {
    return registry.lookupLocalObjects(type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Collection<T> lookupObjectsForLifecycle(Class<T> type) {
    return registry.lookupObjectsForLifecycle(type);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) registry.get(key);
  }

  @Override
  public <T> Map<String, T> lookupByType(Class<T> type) {
    return registry.lookupByType(type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerObject(String key, Object value, Object metadata) throws RegistrationException {
    registry.registerObject(key, value, metadata);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerObject(String key, Object value) throws RegistrationException {
    registry.registerObject(key, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerObjects(Map objects) throws RegistrationException {
    registry.registerObjects(objects);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object unregisterObject(String key) throws RegistrationException {
    return registry.unregisterObject(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  // Registry Metadata
  ////////////////////////////////////////////////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRegistryId() {
    return this.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRemote() {
    return false;
  }

  public Registry getDelegate() {
    return registry;
  }
}


