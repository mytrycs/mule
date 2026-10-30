/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain;

import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;

import com.zernova.runtime.api.notification.ClusterNodeNotification;
import com.zernova.runtime.api.notification.ClusterNodeNotificationListener;
import com.zernova.runtime.api.notification.ConnectionNotification;
import com.zernova.runtime.api.notification.ConnectionNotificationListener;
import com.zernova.runtime.api.notification.CustomNotification;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.ExtensionNotification;
import com.zernova.runtime.api.notification.ManagementNotification;
import com.zernova.runtime.api.notification.ManagementNotificationListener;
import com.zernova.runtime.api.notification.SecurityNotification;
import com.zernova.runtime.api.notification.SecurityNotificationListener;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotification;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.internal.context.DefaultZernovaContextBuilder;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;

/**
 * Builder for domain ZernovaContext instance.
 */
public class DomainZernovaContextBuilder extends DefaultZernovaContextBuilder {

  private final String domainId;

  public DomainZernovaContextBuilder(String domainId) {
    super(DOMAIN);
    this.domainId = domainId;
  }

  @Override
  public ZernovaConfiguration getMuleConfiguration() {
    DefaultZernovaConfiguration defaultMuleConfiguration = new DefaultZernovaConfiguration(true);
    defaultMuleConfiguration.setDomainId(domainId);
    defaultMuleConfiguration.setId(domainId);
    if (executionClassLoader instanceof ZernovaArtifactClassLoader) {
      defaultMuleConfiguration
          .setMinMuleVersion(((ZernovaArtifactClassLoader) executionClassLoader).getArtifactDescriptor().getMinMuleVersion());
    }
    return defaultMuleConfiguration;
  }

  @Override
  protected ServerNotificationManager createNotificationManager() {
    ServerNotificationManager manager = new ServerNotificationManager();
    manager.addInterfaceToType(ZernovaContextNotificationListener.class, ZernovaContextNotification.class);
    manager.addInterfaceToType(SecurityNotificationListener.class, SecurityNotification.class);
    manager.addInterfaceToType(ManagementNotificationListener.class, ManagementNotification.class);
    manager.addInterfaceToType(CustomNotificationListener.class, CustomNotification.class);
    manager.addInterfaceToType(ConnectionNotificationListener.class, ConnectionNotification.class);
    manager.addInterfaceToType(ExceptionNotificationListener.class, ExceptionNotification.class);
    manager.addInterfaceToType(ClusterNodeNotificationListener.class, ClusterNodeNotification.class);
    manager.addInterfaceToType(ExceptionNotificationListener.class, ExtensionNotification.class);
    return manager;
  }
}
