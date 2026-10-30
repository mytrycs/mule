/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.sdk.api.metadata.MetadataCache;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class SdkMetadataCacheAdapter implements MetadataCache {

  private final com.zernova.runtime.api.metadata.MetadataCache delegate;

  public SdkMetadataCacheAdapter(com.zernova.runtime.api.metadata.MetadataCache delegate) {
    this.delegate = delegate;
  }

  @Override
  public void put(Serializable key, Serializable value) {
    delegate.put(key, value);
  }

  @Override
  public void putAll(Map<? extends Serializable, ? extends Serializable> values) {
    delegate.putAll(values);
  }

  @Override
  public <T extends Serializable> Optional<T> get(Serializable key) {
    return delegate.get(key);
  }

  @Override
  public <T extends Serializable> T computeIfAbsent(Serializable key, MetadataCacheValueResolver mappingFunction)
      throws MetadataResolvingException, ConnectionException {
    return delegate.computeIfAbsent(key, new ZernovaMetadataCacheValueResolverAdapter(mappingFunction));
  }

}
