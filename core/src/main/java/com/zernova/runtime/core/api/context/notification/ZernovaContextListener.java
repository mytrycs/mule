/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.core.api.ZernovaContext;

/**
 * Listens to public events raised from {@link ZernovaContext}
 */
public interface ZernovaContextListener {

  /**
   * Notifies the creation of a {@link ZernovaContext} instance right before the initialization.
   *
   * @param context created context
   */
  void onCreation(ZernovaContext context);

  /**
   * Notifies after initialization of a {@link ZernovaContext} instance.
   *
   * @param context  initialized context
   * @param registry the registry of the initialized context
   */
  void onInitialization(ZernovaContext context, Registry registry);

  /**
   * Notifies the stopping of a {@link ZernovaContext} instance.
   *
   * @param context configured context
   */
  void onStart(ZernovaContext context, Registry registry);

  /**
   * Notifies the stopping of a {@link ZernovaContext} instance.
   *
   * @param context configured context
   */
  void onStop(ZernovaContext context, Registry registry);
}
