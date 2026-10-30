/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.runtime.config;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;

/**
 * Produces instances of {@link ConnectionProviderModel}
 *
 * @since 4.8
 */
public interface ConnectionProviderObjectBuilder<C> {

  /**
   * Returns a new instance of the {@link ConnectionProvider} with its fields populated.
   *
   * @param result a {@link ResolverSetResult} to inject the fields of the {@link ConnectionProvider}.
   * @return a new instance
   * @throws {@link ZernovaException}
   */
  public Pair<ConnectionProvider<C>, ResolverSetResult> build(ResolverSetResult result) throws ZernovaException;
}
