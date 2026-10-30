/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.manager;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.module.extension.internal.manager.DefaultExtensionManager;

/**
 * Default implementation of {@link ExtensionManagerFactory} which creates instances of {@link DefaultExtensionManager} and sets
 * them into the owning {@link ZernovaContext}
 *
 * @since 4.0
 */
public final class DefaultExtensionManagerFactory implements ExtensionManagerFactory {

  /**
   * {@inheritDoc}
   */
  @Override
  public ExtensionManager create(ZernovaContext muleContext) {
    ExtensionManager extensionManager = new DefaultExtensionManager();
    muleContext.setExtensionManager(extensionManager);
    try {
      initialiseIfNeeded(extensionManager, muleContext);
    } catch (InitialisationException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not initialise extension manager"), e);
    }

    return extensionManager;
  }
}
