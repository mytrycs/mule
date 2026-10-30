/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.store;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STORE_MANAGER;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreSettings;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZernovaObjectStoreDisposalTestCase extends AbstractZernovaContextTestCase {

  private static final String TEST_OS_NAME = "disposalTest";
  private static final int MAX_ENTRIES = 100;
  private static final long TIMEOUT = 9999;
  private static final String DISPOSABLE_TRANSIENT_USER_STORE_KEY = "DISPOSABLE_TRANSIENT_USER_STORE_KEY";

  private ZernovaObjectStoreManager osm;

  @Mock(extraInterfaces = Disposable.class)
  private ObjectStore disposableStore;

  @Override
  protected void doSetUp() throws Exception {
    osm = ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(OBJECT_STORE_MANAGER);
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(DISPOSABLE_TRANSIENT_USER_STORE_KEY, disposableStore);
    osm.setBaseTransientStoreKey(DISPOSABLE_TRANSIENT_USER_STORE_KEY);
  }

  @Test
  public void disposeMonitoredObjectStores() throws Exception {
    ObjectStore<?> managedObjectStore = osm.createObjectStore(TEST_OS_NAME, ObjectStoreSettings.builder()
        .persistent(false)
        .maxEntries(MAX_ENTRIES)
        .entryTtl(TIMEOUT)
        .expirationInterval(TIMEOUT)
        .build());

    assertNotNull(managedObjectStore);
    assertTrue(managedObjectStore instanceof Disposable);

    doDispose();
    verify((Disposable) disposableStore).dispose();
  }

  private void doDispose() {
    muleContext.dispose();
    muleContext = null;
  }
}
