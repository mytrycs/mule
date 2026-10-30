/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.internal.registry.Registry;

/**
 * Declares {@link ZernovaRegistry} handling methods for {@link ZernovaContext}.
 *
 * @since 4.0
 * @deprecated Use {@link com.zernova.runtime.api.artifact.Registry} instead.
 */
// TODO MULE-13627 Remove downcasts to this interface
@Deprecated
public interface ZernovaContextWithRegistry extends ZernovaContext {

  /**
   * @return the {@link ZernovaRegistry}
   */
  ZernovaRegistry getRegistry();

  /**
   * Sets the {@link Registry} that this context will use.
   * <p>
   * If the {@code registry} is not a {@link ZernovaRegistry} it will be adapted into one.
   * <p>
   * Notice that if the context already has a registry, it will be replaced without taking any particular lifecycle activity over
   * it. That will be up to you.
   *
   * @param registry the new registry to use
   */
  void setRegistry(Registry registry);

}
