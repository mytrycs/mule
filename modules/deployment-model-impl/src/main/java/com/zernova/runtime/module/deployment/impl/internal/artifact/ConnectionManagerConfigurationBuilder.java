/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.artifact;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkNotNull;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONNECTION_MANAGER;
import static com.zernova.runtime.core.privileged.registry.LegacyRegistryUtils.lookupObject;
import static com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactFactoryUtils.isConfigLess;
import static com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactFactoryUtils.withArtifactMuleContext;
import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.util.func.CheckedConsumer;
import com.zernova.runtime.core.internal.connection.CompositeConnectionManager;
import com.zernova.runtime.core.internal.connection.ConnectionManagerAdapter;
import com.zernova.runtime.core.internal.connection.DelegateConnectionManagerAdapter;
import com.zernova.runtime.deployment.model.api.DeployableArtifact;

/**
 * {@link ConfigurationBuilder} implementation which enriches the {@link ZernovaContext}'s registry injecting the correspondent
 * {@link ConnectionManager}
 *
 * @since 4.0
 */
public class ConnectionManagerConfigurationBuilder implements ConfigurationBuilder {

  private final CheckedConsumer<ZernovaContext> muleContextConfigurer;

  ConnectionManagerConfigurationBuilder(DeployableArtifact parentArtifact) {
    checkNotNull(parentArtifact, "'parentArtifact' can't be null");

    muleContextConfigurer = muleContext -> withArtifactMuleContext(parentArtifact, parentContext -> {
      if (isConfigLess(parentArtifact)) {
        registerDefaultConnectionManager(muleContext);
      } else {
        ConnectionManagerAdapter parentConnectionManager = lookupObject(parentContext, OBJECT_CONNECTION_MANAGER);
        if (parentConnectionManager != null) {
          ConnectionManager connectionManager =
              new CompositeConnectionManager(new DelegateConnectionManagerAdapter(muleContext), parentConnectionManager);
          registerConnectionManager(muleContext, connectionManager);
        } else {
          registerDefaultConnectionManager(muleContext);
        }
      }
    });
  }

  ConnectionManagerConfigurationBuilder() {
    muleContextConfigurer = this::registerDefaultConnectionManager;
  }

  @Override
  public void configure(ZernovaContext muleContext) throws ConfigurationException {
    try {
      muleContextConfigurer.acceptChecked(muleContext);
    } catch (Throwable e) {
      throw new ConfigurationException(createStaticMessage("An error occurred trying to register the Mule Connection Manager"),
                                       e);
    }
  }

  private void registerDefaultConnectionManager(ZernovaContext muleContext) {
    registerConnectionManager(muleContext, new DelegateConnectionManagerAdapter(muleContext));
  }

  private void registerConnectionManager(ZernovaContext muleContext, ConnectionManager connectionManager) {
    muleContext.getCustomizationService().overrideDefaultServiceImpl(OBJECT_CONNECTION_MANAGER, connectionManager);
  }

  @Override
  public void addServiceConfigurator(ServiceConfigurator serviceConfigurator) {
    // Nothing to do
  }
}
