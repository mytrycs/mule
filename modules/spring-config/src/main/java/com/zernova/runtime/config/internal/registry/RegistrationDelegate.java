/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.registry;

import com.zernova.runtime.core.privileged.registry.RegistrationException;

import java.util.Map;

/**
 * Delegate interface for object registration
 *
 * @since 3.7.0
 */
interface RegistrationDelegate {

  void registerObject(String key, Object value) throws RegistrationException;

  void registerObject(String key, Object value, Object metadata) throws RegistrationException;

  void registerObjects(Map<String, Object> objects) throws RegistrationException;

  Object unregisterObject(String key) throws RegistrationException;
}
