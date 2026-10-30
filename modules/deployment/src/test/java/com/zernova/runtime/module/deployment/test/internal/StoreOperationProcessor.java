/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.store.ObjectStoreSettings;
import com.zernova.runtime.core.api.event.CoreEvent;

import jakarta.inject.Inject;

/**
 * Component used on deployment test that require policies to check that they are invoked
 * <p/>
 * Static state must be reset before each test is executed
 */
public class StoreOperationProcessor extends AbstractComponent implements com.zernova.runtime.core.api.processor.Processor {

  @Inject
  ObjectStoreManager objectStoreManager;

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    ObjectStore<String> myPartition =
        objectStoreManager.getOrCreateObjectStore("myPartition", ObjectStoreSettings.builder().persistent(true).build());
    String key = "value";
    if (myPartition.contains(key)) {
      myPartition.remove(key);
    }
    myPartition.store(key, key);
    return event;
  }
}
