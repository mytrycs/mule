/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.internal.cache;

import static com.zernova.test.allure.AllureConstants.SdkToolingSupport.SDK_TOOLING_SUPPORT;
import static com.zernova.test.allure.AllureConstants.SdkToolingSupport.MetadataTypeResolutionStory.METADATA_SERVICE;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.api.metadata.MetadataCache;
import com.zernova.runtime.api.store.ObjectDoesNotExistException;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SDK_TOOLING_SUPPORT)
@Story(METADATA_SERVICE)
public class DefaultMetadataCacheManagerTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  private static final String SOME_KEY = "1874947571-1840879217-380895431-1745289126";
  private static final String OTHER_KEY = "1874947571-1840879217-123123123-1745289126";

  @Mock
  private ObjectStoreManager objectStoreManager;

  @Mock
  private LockFactory lockFactory;

  @Mock
  private Lock mockLock;

  @Mock
  private ObjectStore<MetadataCache> objectStore;

  @Mock
  private MetadataCache mockCache;

  @InjectMocks
  private final DefaultPersistentMetadataCacheManager cacheManager = new DefaultPersistentMetadataCacheManager();

  @Before
  public void setUp() {
    when(lockFactory.createLock(anyString())).thenReturn(mockLock);
    when(objectStoreManager.getOrCreateObjectStore(anyString(), any()))
        .thenReturn(objectStore);

    cacheManager.start();
  }

  @Test
  public void createCacheWhenMissingKey() throws ObjectStoreException {
    when(objectStore.contains(SOME_KEY)).thenReturn(false);

    MetadataCache cache = cacheManager.getOrCreateCache(SOME_KEY);
    assertThat(cache, instanceOf(DefaultMetadataCache.class));
    verify(objectStore).contains(SOME_KEY);
    verify(objectStore, never()).retrieve(SOME_KEY);
    verify(objectStore).store(SOME_KEY, cache);
  }

  @Test
  public void retrieveCacheWhenExists() throws ObjectStoreException {
    when(objectStore.contains(SOME_KEY)).thenReturn(true);
    when(objectStore.retrieve(SOME_KEY)).thenReturn(mockCache);

    MetadataCache actual = cacheManager.getOrCreateCache(SOME_KEY);
    assertThat(actual, is(mockCache));
    verify(objectStore).contains(SOME_KEY);
    verify(objectStore).retrieve(SOME_KEY);
    verify(objectStore, never()).store(anyString(), any(MetadataCache.class));
  }

  @Test
  public void updateCache() throws ObjectStoreException {
    when(objectStore.contains(SOME_KEY)).thenReturn(true);
    when(objectStore.remove(SOME_KEY)).thenReturn(mockCache);

    DefaultMetadataCache cache = new DefaultMetadataCache();
    cacheManager.updateCache(SOME_KEY, cache);

    verify(objectStore).contains(SOME_KEY);
    verify(objectStore).remove(SOME_KEY);
    verify(objectStore).store(SOME_KEY, cache);
    verify(objectStore, never()).retrieve(SOME_KEY);
  }

  @Test
  public void disposeExactId() throws ObjectStoreException {
    when(objectStore.remove(SOME_KEY)).thenReturn(mockCache);

    cacheManager.dispose(SOME_KEY);

    verify(objectStore).remove(SOME_KEY);
    verify(objectStore, never()).contains(SOME_KEY);
    verify(objectStore, never()).retrieve(SOME_KEY);
  }

  @Test
  public void disposePartialId() throws ObjectStoreException {
    final String partialId = "1874947571-1840879217";

    when(objectStore.allKeys()).thenReturn(Arrays.asList(SOME_KEY, OTHER_KEY));
    when(objectStore.remove(partialId)).thenThrow(new ObjectDoesNotExistException());

    cacheManager.dispose(partialId);

    verify(objectStore).remove(SOME_KEY);
    verify(objectStore).remove(OTHER_KEY);
  }

  @Test
  public void clearMetadataCaches() throws ObjectStoreException {
    cacheManager.dispose("");

    verify(objectStore).clear();
  }
}
