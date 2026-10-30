/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.store.ObjectStore;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.Serializable;

import jakarta.inject.Inject;

/**
 * Lookup an {@link ObjectStore} from the registry.
 */
public class ObjectStoreFromRegistryFactoryBean extends AbstractFactoryBean<ObjectStore<Serializable>> {

  private String objectStoreName;

  @Inject
  private Registry registry;

  public ObjectStoreFromRegistryFactoryBean(String name) {
    super();
    objectStoreName = name;
  }

  @Override
  public Class<?> getObjectType() {
    return ObjectStore.class;
  }

  @Override
  protected ObjectStore<Serializable> createInstance() throws Exception {
    return registry.<ObjectStore>lookupByName(objectStoreName).get();
  }
}
