/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.internal;

import com.zernova.runtime.module.boot.api.ZernovaContainer;

/**
 * A factory for {@link ZernovaContainer} instances. Responsible for choosing the right implementation class and setting up its
 * {@link ClassLoader}.
 *
 * @since 4.6
 */
public interface ZernovaContainerFactory {

  /**
   * Creates the {@link ZernovaContainer} instance.
   *
   * @param args Any arguments to forward to the Container (that have not been yet processed by the bootstrapping application).
   * @return A new {@link ZernovaContainer} instance.
   * @throws Exception If there is any problem creating the {@link ZernovaContainer} instance. The bootstrapping application should
   *                   exit immediately.
   */
  ZernovaContainer create(String[] args) throws Exception;
}
