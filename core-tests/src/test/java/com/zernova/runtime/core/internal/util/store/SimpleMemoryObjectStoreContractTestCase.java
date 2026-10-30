/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.store;

import static org.junit.Assert.fail;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.api.store.SimpleMemoryObjectStore;
import com.zernova.tck.core.util.store.TemplateObjectStoreContractTestCase;
import com.zernova.tck.testmodels.fruit.Banana;

import java.io.Serializable;

import org.junit.Test;

public class SimpleMemoryObjectStoreContractTestCase extends TemplateObjectStoreContractTestCase {

  @Override
  public ObjectStore<Serializable> getObjectStore() {
    return new SimpleMemoryObjectStore<>();
  }

  @Override
  public Serializable getStorableValue() {
    return new Banana();
  }

  @Test
  public void storesNullValue() throws Exception {
    try {
      getObjectStore().store("key", null);
      fail("store() called with null value must throw ObjectStoreException");
    } catch (ObjectStoreException ose) {
      // this one was expected
    }
  }
}
