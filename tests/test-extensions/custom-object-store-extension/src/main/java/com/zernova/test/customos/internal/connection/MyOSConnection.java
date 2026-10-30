/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.customos.internal.connection;

import static com.zernova.test.customos.internal.MyOSConnector.VALUES;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.store.ObjectStoreSettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyOSConnection implements ObjectStoreManager {

  private ObjectStore<TypedValue<String>> objectStore = new MapObjectStore();

  @Override
  public <T extends ObjectStore<? extends Serializable>> T getObjectStore(String name) {
    return (T) objectStore;
  }

  @Override
  public <T extends ObjectStore<? extends Serializable>> T createObjectStore(String name, ObjectStoreSettings settings) {
    return (T) objectStore;
  }

  @Override
  public <T extends ObjectStore<? extends Serializable>> T getOrCreateObjectStore(String name, ObjectStoreSettings settings) {
    return (T) objectStore;
  }

  @Override
  public void disposeStore(String name) throws ObjectStoreException {}

  private class MapObjectStore implements ObjectStore<TypedValue<String>> {

    @Override
    public boolean contains(String key) throws ObjectStoreException {
      return VALUES.containsKey(key);
    }

    @Override
    public void store(String key, TypedValue<String> value) throws ObjectStoreException {
      VALUES.put(key, value);
    }

    @Override
    public TypedValue<String> retrieve(String key) throws ObjectStoreException {
      return VALUES.get(key);
    }

    @Override
    public TypedValue<String> remove(String key) throws ObjectStoreException {
      return VALUES.remove(key);
    }

    @Override
    public boolean isPersistent() {
      return false;
    }

    @Override
    public void clear() throws ObjectStoreException {
      VALUES.clear();
    }

    @Override
    public void open() throws ObjectStoreException {

    }

    @Override
    public void close() throws ObjectStoreException {

    }

    @Override
    public List<String> allKeys() throws ObjectStoreException {
      return new ArrayList<>(VALUES.keySet());
    }

    @Override
    public Map<String, TypedValue<String>> retrieveAll() throws ObjectStoreException {
      return VALUES;
    }
  }
}
