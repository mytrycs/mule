/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.tooling;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.failure;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.internal.event.NullEventFactory.getNullEvent;

import static java.lang.String.format;

import com.zernova.api.annotation.NoExtend;
import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.connectivity.ConnectivityTestingStrategy;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;

import jakarta.inject.Inject;

/**
 * Implementation of {@code ConnectivityTestingStrategy} that can do connectivity testing over components creates with extensions
 * API.
 *
 * @since 4.0
 */
@NoExtend
@NoInstantiate
public class ExtensionConnectivityTestingStrategy implements ConnectivityTestingStrategy {

  @Inject
  private ConnectionManager connectionManager;

  @Inject
  private ExpressionManager expressionManager;

  public ExtensionConnectivityTestingStrategy() {}

  /**
   * Used for testing purposes
   *
   * @param connectionManager the {@link ConnectionManager} to use for validating the connection.
   */
  ExtensionConnectivityTestingStrategy(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionValidationResult testConnectivity(Object connectivityTestingObject) {
    CoreEvent initialiserEvent = null;
    try {
      initialiserEvent = getNullEvent();
      if (connectivityTestingObject instanceof ConnectionProviderResolver) {
        ConnectionProviderResolver<?> resolver = (ConnectionProviderResolver<?>) connectivityTestingObject;
        try (ValueResolvingContext ctx = ValueResolvingContext.builder(initialiserEvent, expressionManager).build()) {
          ConnectionProvider connectionProvider = resolver.resolve(ctx).getFirst();
          return connectionManager.testConnectivity(connectionProvider);
        }
      } else if (connectivityTestingObject instanceof ConfigurationProvider) {
        ConfigurationProvider configurationProvider = (ConfigurationProvider) connectivityTestingObject;
        ConfigurationInstance configurationInstance = configurationProvider.get(initialiserEvent);
        return connectionManager.testConnectivity(configurationInstance);
      } else {
        throw new ZernovaRuntimeException(createStaticMessage(
                                                           format("testConnectivity was invoked with an object type %s not supported.",
                                                                  connectivityTestingObject.getClass().getName())));
      }
    } catch (Exception e) {
      return failure("Failed to obtain connectivity testing object", e);
    } finally {
      if (initialiserEvent != null) {
        ((BaseEventContext) initialiserEvent.getContext()).success();
      }
    }
  }

  /**
   * @return true whenever there's a {@code ConfigurationProvider} in the configuration, false otherwise.
   */
  @Override
  public boolean accepts(Object connectivityTestingObject) {
    return connectivityTestingObject instanceof ConnectionProviderResolver
        || connectivityTestingObject instanceof ConfigurationProvider;
  }

}
