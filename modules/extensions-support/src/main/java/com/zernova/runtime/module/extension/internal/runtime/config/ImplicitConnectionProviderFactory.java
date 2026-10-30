/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;

import java.util.Optional;

/**
 * Creates {@link ConnectionProvider} instances which can be implicitly derived from a given {@link ExtensionModel}.
 *
 * @since 4.0
 */
public interface ImplicitConnectionProviderFactory {

  /**
   * Creates a new {@link ConnectionProvider} based on the {@link ConnectionProviderModel} available to the {@code extensionModel}
   * and {@code configurationModel}
   *
   * @param configName the name of the configuration that will own the returned {@link ConnectionProvider}
   * @param event      the {@link CoreEvent} that will be used to evaluate any default parameters that requires resolving an
   *                   expression
   * @param <T>        the generic type of the connections that the returned provider produces
   * @return a {@link ConnectionProvider}
   * @throws IllegalArgumentException if the {@code extensionModel} doesn't have any {@link ConnectionProviderModel} which can be
   *                                  used implicitly
   */
  <T> Pair<ConnectionProvider<T>, ResolverSetResult> createImplicitConnectionProvider(String configName, CoreEvent event);

  /**
   * @return whether the {@link ResolverSet} used for resolving the {@link ConnectionProvider} is dynamic or not.
   */
  boolean isDynamic();

  /**
   * @return the {@link ResolverSet} used for resolving the {@link ConnectionProvider} parameters
   */
  Optional<ResolverSet> getResolverSet();

}
