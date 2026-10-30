/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.metadata.cache.lazy;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.MetadataCache;
import com.zernova.runtime.metadata.internal.cache.MetadataCacheManager;

import java.util.function.Function;

import jakarta.inject.Inject;

public class DelegateMetadataCacheManager implements MetadataCacheManager, Initialisable {

  private final Function<Registry, MetadataCacheManager> metadataCacheManagerSupplier;

  @Inject
  private Registry registry;

  private MetadataCacheManager metadataCacheManagerDelegate;

  public DelegateMetadataCacheManager(Function<Registry, MetadataCacheManager> metadataCacheManagerSupplier) {
    this.metadataCacheManagerSupplier = metadataCacheManagerSupplier;
  }

  @Override
  public void initialise() throws InitialisationException {
    this.metadataCacheManagerDelegate = metadataCacheManagerSupplier.apply(registry);
  }

  @Override
  public MetadataCache getOrCreateCache(String id) {
    return metadataCacheManagerDelegate.getOrCreateCache(id);
  }

  @Override
  public void updateCache(String id, MetadataCache cache) {
    metadataCacheManagerDelegate.updateCache(id, cache);
  }

  @Override
  public void dispose(String id) {
    metadataCacheManagerDelegate.dispose(id);
  }
}
