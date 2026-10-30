/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import com.zernova.api.annotation.NoImplement;

@NoImplement
public interface HasConnectionProviderDeclarer {

  /**
   * Adds a connection provider of the given {@code name}
   *
   * @param name a non blank name
   * @return a {@link ConnectionProviderDeclarer} which allows describing the created provider
   */
  ConnectionProviderDeclarer withConnectionProvider(String name);

  /**
   * Adds a {@link ConnectionProviderDeclaration} by receiving a {@code declarer} which describes it
   *
   * @param declarer a {@link ConnectionProviderDeclarer}
   */
  void withConnectionProvider(ConnectionProviderDeclarer declarer);
}
