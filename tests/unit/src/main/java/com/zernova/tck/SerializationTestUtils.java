/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.store.ObjectStoreSettings;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;
import com.zernova.runtime.core.privileged.registry.RegistrationException;

public abstract class SerializationTestUtils {

  private static final String key = "SerializationTestComponentKey";

  public static <T extends Exception> T testException(T exception, ZernovaContext muleContext) {
    ObjectStore<T> os;
    try {
      os = getObjectStore(muleContext);
    } catch (RegistrationException e) {
      throw new RuntimeException(e);
    }

    try {
      os.store(key, exception);
      return os.retrieve(key);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        os.clear();
      } catch (ObjectStoreException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static <T extends Exception> ObjectStore<T> getObjectStore(ZernovaContext muleContext) throws RegistrationException {
    return ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(ObjectStoreManager.class)
        .createObjectStore("SerializationTestUtils", ObjectStoreSettings.builder().persistent(true).build());
  }

  public static ObjectSerializer getJavaSerializerWithMockContext() {
    ZernovaContext muleContext = mock(ZernovaContext.class, RETURNS_DEEP_STUBS);
    when(muleContext.getExecutionClassLoader()).thenReturn(ObjectSerializer.class.getClassLoader());
    return getJavaSerializer(muleContext);
  }

  public static ObjectSerializer addJavaSerializerToMockMuleContext(ZernovaContext mockMuleContext) {
    ObjectSerializer objectSerializer = getJavaSerializer(mockMuleContext);
    when(mockMuleContext.getObjectSerializer()).thenReturn(objectSerializer);
    return objectSerializer;
  }

  public static ObjectSerializer getJavaSerializer(ZernovaContext muleContext) {
    return new JavaObjectSerializer(muleContext.getExecutionClassLoader());
  }

}
