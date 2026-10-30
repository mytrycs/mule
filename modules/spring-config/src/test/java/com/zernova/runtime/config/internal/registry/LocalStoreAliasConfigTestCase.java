/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.registry;

import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_IN_MEMORY_OBJECT_STORE_KEY;
import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_PERSISTENT_OBJECT_STORE_KEY;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_LOCAL_STORE_IN_MEMORY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_LOCAL_STORE_PERSISTENT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_QUEUE_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STORE_MANAGER;
import static com.zernova.runtime.core.internal.context.DefaultZernovaContext.LOCAL_OBJECT_STORE_MANAGER_KEY;
import static com.zernova.runtime.core.internal.context.DefaultZernovaContext.LOCAL_QUEUE_MANAGER_KEY;

import static java.util.Collections.emptyMap;

import static org.junit.Assert.assertSame;

import com.zernova.runtime.config.internal.ArtifactAstConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;

public class LocalStoreAliasConfigTestCase extends AbstractZernovaContextTestCase {

  @Override
  protected ConfigurationBuilder getBuilder() throws Exception {
    return new ArtifactAstConfigurationBuilder(emptyArtifact(), emptyMap());
  }

  @Test
  public void inMemoryObjectStore() throws Exception {
    this.testSame(BASE_IN_MEMORY_OBJECT_STORE_KEY, OBJECT_LOCAL_STORE_IN_MEMORY);
  }

  @Test
  public void persistentObjectStore() throws Exception {
    this.testSame(BASE_PERSISTENT_OBJECT_STORE_KEY, OBJECT_LOCAL_STORE_PERSISTENT);
  }

  @Test
  public void queueManager() throws Exception {
    this.testSame(OBJECT_QUEUE_MANAGER, LOCAL_QUEUE_MANAGER_KEY);
    assertSame(muleContext.getQueueManager(),
               ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(LOCAL_QUEUE_MANAGER_KEY));
  }

  @Test
  public void objectStoreManager() throws Exception {
    this.testSame(OBJECT_STORE_MANAGER, LOCAL_OBJECT_STORE_MANAGER_KEY);
    assertSame(muleContext.getObjectStoreManager(),
               ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(LOCAL_OBJECT_STORE_MANAGER_KEY));
  }

  private void testSame(String key1, String key2) {
    Object obj1 = ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(key1);
    Object obj2 = ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(key2);
    assertSame(obj1, obj2);
  }
}
