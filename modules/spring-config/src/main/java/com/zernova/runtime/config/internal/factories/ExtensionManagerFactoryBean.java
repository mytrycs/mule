/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;

import org.springframework.beans.factory.FactoryBean;

import jakarta.inject.Inject;

/**
 * A {@link FactoryBean} which returns the {@link ExtensionManager} obtained through {@link ZernovaContext#getExtensionManager()}.
 * The purpose of that is to put the extensionManager in the context, even though it makes exist before it.
 *
 * @since 3.7.0
 */
public class ExtensionManagerFactoryBean implements FactoryBean<ExtensionManager> {

  private final ZernovaContext muleContext;

  @Inject
  public ExtensionManagerFactoryBean(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  @Override
  public ExtensionManager getObject() throws Exception {
    return muleContext.getExtensionManager();
  }

  @Override
  public Class<?> getObjectType() {
    return ExtensionManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
