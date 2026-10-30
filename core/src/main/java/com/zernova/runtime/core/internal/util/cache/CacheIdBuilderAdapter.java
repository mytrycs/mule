/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.cache;

import java.util.List;

/**
 * Helper interface to unify both {@link com.zernova.runtime.core.internal.value.cache.ValueProviderCacheId} and
 * {@link com.zernova.runtime.core.privileged.metadata.cache.MetadataCacheId} construction.
 *
 * @param <K> the cache id this builder creates.
 */
public interface CacheIdBuilderAdapter<K> {

  CacheIdBuilderAdapter<K> withSourceElementName(String name);

  CacheIdBuilderAdapter<K> withHashValue(int value);

  CacheIdBuilderAdapter<K> containing(List<K> parts);

  K build();

}
