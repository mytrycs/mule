/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.store;

import com.zernova.runtime.api.store.ObjectStore;

import java.io.Serializable;

/**
 * Create the default object stores
 */
public interface DefaultObjectStoreFactory {

  /**
   * Creates an in memory object store for mule components
   *
   * @return in memory object store
   */
  ObjectStore<Serializable> createDefaultInMemoryObjectStore();

  /**
   * Creates a persistent object store for mule components
   *
   * @return persistent object store
   */
  ObjectStore<Serializable> createDefaultPersistentObjectStore();
}
