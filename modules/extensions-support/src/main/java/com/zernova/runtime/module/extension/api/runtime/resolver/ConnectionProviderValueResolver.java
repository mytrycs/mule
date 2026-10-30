/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.runtime.resolver;

import static java.util.Optional.empty;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.module.extension.api.runtime.config.ConnectionProviderObjectBuilder;

import java.util.Optional;

/**
 * Provides de capability to obtain (if there is any) the {@link ResolverSet} used to resolve the event.
 *
 * @param <C> the generic type of the connections to be handled
 * @since 4.0
 */
public interface ConnectionProviderValueResolver<C> extends ValueResolver<Pair<ConnectionProvider<C>, ResolverSetResult>> {

  /**
   * @return the {@link ResolverSet} that will be used to resolve the values from a given event if there is any.
   */
  default Optional<ResolverSet> getResolverSet() {
    return empty();
  }

  default Optional<ConnectionProviderObjectBuilder<C>> getObjectBuilder() {
    return empty();
  }
}
