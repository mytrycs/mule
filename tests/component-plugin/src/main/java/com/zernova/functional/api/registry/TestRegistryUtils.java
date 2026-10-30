/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.registry;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.privileged.registry.LegacyRegistryUtils;
import com.zernova.runtime.core.privileged.registry.RegistrationException;

/**
 * Provides a way for integration tests to access certain functionality of the internal {@link ZernovaRegistry}.
 *
 * @since 4.0
 *
 * @deprecated inject {@link Registry} where possible instead of using this utility.
 */
@Deprecated
public class TestRegistryUtils {

  /**
   * Registers an object in the registry with a key.
   *
   * @param key   the key to store the value against. This is a non-null value
   * @param value the object to store in the registry. This is a non-null value
   * @throws ZernovaRuntimeException wrapping a RegistrationException if an object with the same key already exists
   */
  public static void registerObject(ZernovaContext context, String key, Object value) {
    try {
      LegacyRegistryUtils.registerObject(context, key, value);
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }


}
