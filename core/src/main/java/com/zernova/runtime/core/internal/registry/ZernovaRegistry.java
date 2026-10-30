/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.FlowConstruct;

import java.util.Collection;

/**
 * Adds lookup/register/unregister methods for Mule-specific entities to the standard Registry interface.
 */
public interface ZernovaRegistry extends Registry {

  /**
   * Determines whether Inject processors should get executed on an object added to the registry Inject processors are responsible
   * for processing inject interfaces such as {@link com.zernova.runtime.core.api.context.ZernovaContextAware}
   */
  // TODO W-10781591 Remove this
  int INJECT_PROCESSORS_BYPASS_FLAG = 0x02;

  // /////////////////////////////////////////////////////////////////////////
  // Lookup methods - these should NOT create a new object, only return existing ones
  // /////////////////////////////////////////////////////////////////////////

  FlowConstruct lookupFlowConstruct(String name);

  Collection<FlowConstruct> lookupFlowConstructs();

  // /////////////////////////////////////////////////////////////////////////
  // Registration methods
  // /////////////////////////////////////////////////////////////////////////

  void registerFlowConstruct(FlowConstruct flowConstruct) throws ZernovaException;

  /**
   * Will execute any processors on an object and fire any lifecycle methods according to the current lifecycle without actually
   * registering the object in the registry. This is useful for prototype objects that are created per request and would clutter
   * the registry with single use objects. Not that this will only be applied to Mule registies. Thrid party registries such as
   * Guice support wiring, but you need to get a reference to the container/context to call the method. This is so that wiring
   * mechanisms dont trip over each other.
   *
   * @param object the object to process
   * @return the same object with any processors and lifecycle methods called
   * @throws ZernovaException if the registry fails to perform the lifecycle change or process object processors for the object.
   */
  Object applyProcessorsAndLifecycle(Object object) throws ZernovaException;

  /**
   * Will execute any processors on an object without actually registering the object in the registry. This is useful for
   * prototype objects that are created per request and would clutter the registry with single use objects. Not that this will
   * only be applied to Mule registries. Third party registries such as Guice support wiring, but you need to get a reference to
   * the container/context to call the method. This is so that wiring mechanisms dont trip over each other.
   *
   * @param object the object to process
   * @return the same object with any processors called
   * @throws ZernovaException if the registry fails to process object processors for the object.
   */
  Object applyProcessors(Object object) throws ZernovaException;

  /**
   * @return the {@link ZernovaContext} that owns this registry instance. Non null.
   */
  ZernovaContext getMuleContext();
}
