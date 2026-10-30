/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.manager;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;

/**
 * Factory to create instances of {@link ExtensionManager}
 *
 * @since 4.0
 */
@NoImplement
public interface ExtensionManagerFactory {

  /**
   * Creates a new {@link ExtensionManager}
   *
   * @param muleContext the owning {@link ZernovaContext}
   * @return a non {@code null} {@link ExtensionManager}
   */
  ExtensionManager create(ZernovaContext muleContext);
}
