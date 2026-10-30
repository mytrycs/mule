/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.registry;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;

import java.util.Collection;

/**
 * Provides a way for privileged API clients to access certain functionality of the internal {@link ZernovaRegistry}.
 *
 * @since 4.0
 * @deprecated inject {@link Registry} where possible instead of using this utility.
 */
@Deprecated
public final class LegacyRegistryUtils {

  private LegacyRegistryUtils() {
    // Nothing to do
  }

  /**
   * Look up a single object by name.
   *
   * @return object or null if not found
   */
  public static <T> T lookupObject(ZernovaContext context, String key) {
    return getRegistry(context).lookupObject(key);
  }

  /**
   * Look up a single object by type.
   *
   * @return object or null if not found
   * @throws RegistrationException if more than one object is found.
   */
  public static <T> T lookupObject(ZernovaContext context, Class<T> clazz) throws RegistrationException {
    return getRegistry(context).lookupObject(clazz);
  }

  /**
   * Look up all objects of a given type.
   *
   * @return collection of objects or empty collection if none found
   */
  public static <T> Collection<T> lookupObjects(ZernovaContext context, Class<T> clazz) {
    return getRegistry(context).lookupObjects(clazz);
  }

  /**
   * Look up all objects of a given type that lifecycle should be applied to. This method differs from
   * {@link #lookupObjects(Class)} in that it allows implementations to provide an alternative implementation of lookup for
   * lifecycle. For example only returning pre-existing objects and not creating new ones on the fly.
   *
   * @return collection of objects or empty collection if none found
   */
  public static <T> Collection<T> lookupObjectsForLifecycle(ZernovaContext context, Class<T> clazz) {
    return getRegistry(context).lookupObjectsForLifecycle(clazz);
  }

  /**
   * Registers an object in the registry with a key.
   *
   * @param key   the key to store the value against. This is a non-null value
   * @param value the object to store in the registry. This is a non-null value
   * @throws RegistrationException if an object with the same key already exists
   */
  public static void registerObject(ZernovaContext context, String key, Object object) throws RegistrationException {
    getRegistry(context).registerObject(key, object);
  }

  /**
   * Registers an object in the registry with a key.
   *
   * @param key      the key to store the value against. This is a non-null value
   * @param object   the object to store in the registry. This is a non-null value
   * @param metadata an implementation specific argument that can be passed into the method
   * @throws RegistrationException if an object with the same key already exists
   *
   * @deprecated Use {@link #registerObject(ZernovaContext, String, Object)} instead.
   */
  // TODO W-10781591 remove this
  @Deprecated
  public static void registerObject(ZernovaContext context, String key, Object object, Object metadata)
      throws RegistrationException {
    getRegistry(context).registerObject(key, object, metadata);
  }

  /**
   * @return whether the bean for the given key is declared as a singleton.
   */
  public static boolean isSingleton(ZernovaContext context, String key) {
    return getRegistry(context).isSingleton(key);
  }

  /**
   * Will fire any lifecycle methods according to the current lifecycle without actually registering the object in the registry.
   * This is useful for prototype objects that are created per request and would clutter the registry with single use objects.
   *
   * @param object the object to process
   * @return either the same object but with the lifecycle applied or a proxy to it
   * @throws ZernovaException if the registry fails to perform the lifecycle change for the object.
   */
  public static Object applyLifecycle(ZernovaContext context, Object object) throws ZernovaException {
    return getRegistry(context).applyLifecycle(object);
  }

  /**
   * Will remove an object by name from the registry. By default the registry must apply all remaining lifecycle phases to the
   * object when it is removed.
   *
   * @param key the name or key of the object to remove from the registry
   * @return the unregistered object or {@code null} if no object was registered under that key
   * @throws RegistrationException if there is a problem unregistering the object. Typically this will be because the object's
   *                               lifecycle threw an exception
   */
  public static Object unregisterObject(ZernovaContext context, String key) throws RegistrationException {
    return getRegistry(context).unregisterObject(key);
  }

  /**
   * @return the object to use a lock when synchronizing access to the context's registry.
   */
  public static Object getRegistryLock(ZernovaContext context) {
    return getRegistry(context);
  }

  private static ZernovaRegistry getRegistry(ZernovaContext context) {
    return ((ZernovaContextWithRegistry) context).getRegistry();
  }
}
