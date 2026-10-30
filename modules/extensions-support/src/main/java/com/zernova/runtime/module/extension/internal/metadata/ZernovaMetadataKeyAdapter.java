/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataProperty;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Adapts a {@link com.zernova.sdk.api.metadata.MetadataKey} into a {@link MetadataKey}
 *
 * @since 4.5.0
 */
public class ZernovaMetadataKeyAdapter implements MetadataKey {

  private final com.zernova.sdk.api.metadata.MetadataKey delegate;

  public ZernovaMetadataKeyAdapter(com.zernova.sdk.api.metadata.MetadataKey delegate) {
    this.delegate = delegate;
  }

  @Override
  public <T extends MetadataProperty> Optional<T> getMetadataProperty(Class<T> propertyType) {
    return delegate.getMetadataProperty(propertyType);
  }

  @Override
  public Set<MetadataProperty> getProperties() {
    return delegate.getProperties();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public String getDisplayName() {
    return delegate.getDisplayName();
  }

  @Override
  public Set<MetadataKey> getChilds() {
    Set<MetadataKey> metadataKeys = new HashSet<>();
    delegate.getChilds().forEach(metadataKey -> metadataKeys.add(new ZernovaMetadataKeyAdapter(metadataKey)));
    return metadataKeys;
  }

  @Override
  public String getPartName() {
    return delegate.getPartName();
  }
}
