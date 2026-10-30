/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.os;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.store.ObjectDoesNotExistException;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.store.ObjectStoreSettings;
import com.zernova.runtime.core.api.util.UUID;

import java.io.Serializable;

import jakarta.inject.Inject;

public class UsingObjectStoreOperations implements Initialisable {
  
  @Inject
  private ObjectStoreManager objectStoreManager;

  private ObjectStore<Serializable> os;

  public void initialise() {
    os = objectStoreManager.getOrCreateObjectStore("osName" + UUID.getUUID(), ObjectStoreSettings.builder().persistent(true).build());
  }

  public void useObjectStore() {
    try {
      try {
        os.remove("valueKey");
      } catch (ObjectDoesNotExistException ose) {}
      os.store("valueKey", new UsingObjectStoreData("Hello World!"));
      os.retrieve("valueKey");
    } catch (ObjectStoreException ose) {
      throw new RuntimeException(ose);
    }
  }
}
