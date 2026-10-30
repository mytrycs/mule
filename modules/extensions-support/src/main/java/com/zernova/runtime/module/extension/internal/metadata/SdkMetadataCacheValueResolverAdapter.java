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
import com.zernova.sdk.api.metadata.MetadataCache.MetadataCacheValueResolver;

import java.io.Serializable;

public class SdkMetadataCacheValueResolverAdapter implements MetadataCacheValueResolver {

  private final com.zernova.runtime.api.metadata.MetadataCache.MetadataCacheValueResolver delegate;

  public SdkMetadataCacheValueResolverAdapter(com.zernova.runtime.api.metadata.MetadataCache.MetadataCacheValueResolver delegate) {
    this.delegate = delegate;
  }

  @Override
  public Serializable compute(Serializable key) throws MetadataResolvingException, ConnectionException {
    return delegate.compute(key);
  }
}
