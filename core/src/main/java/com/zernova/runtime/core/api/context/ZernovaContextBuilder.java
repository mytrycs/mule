/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.lifecycle.LifecycleManager;
import com.zernova.runtime.core.internal.context.DefaultZernovaContextBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Builder that is used to build instances of {@link ZernovaContext}. Implementing classes are stateful and should provide public
 * chainable setters for configuring the builder instance and no public getters.
 */
@NoImplement
public interface ZernovaContextBuilder {

  /**
   * Builds a new {@link ZernovaContext} instance using configured builder instance. Does not initialise or start ZernovaContext, only
   * constructs the instance.
   */
  ZernovaContext buildMuleContext();

  void setNotificationManager(ServerNotificationManager notificationManager);

  void setLifecycleManager(LifecycleManager lifecycleManager);

  void setMuleConfiguration(ZernovaConfiguration muleConfiguration);

  /**
   * @param executionClassLoader classloader to use on the created context. Non null.
   */
  void setExecutionClassLoader(ClassLoader executionClassLoader);

  /**
   * @param objectSerializer object serializer to use on the created context. Non null.
   */
  void setObjectSerializer(ObjectSerializer objectSerializer);

  /**
   * @param errorTypeRepository error type repository to be used in the context, is not configured a default one will be used.
   * @deprecated error type repo is determined from the application, it may not be set. This is a no-op since 4.4.
   */
  @Deprecated
  void setErrorTypeRepository(ErrorTypeRepository errorTypeRepository);


  /**
   * Creates a new {@link ZernovaContextBuilder} instance
   *
   * @param artifactType type of the artifact the owns the created context.
   * @return a new builder instance
   */
  static ZernovaContextBuilder builder(ArtifactType artifactType) {
    return new DefaultZernovaContextBuilder(artifactType);
  }

  void setDeploymentProperties(Optional<Properties> properties);

  void setListeners(List<ZernovaContextListener> listeners);

  /**
   * Sets the {@link ArtifactCoordinates} for the deployed app
   *
   * @param artifactCoordinates the app's {@link ArtifactCoordinates}
   * @since 4.5.0
   */
  void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates);

  /**
   * Sets the {@link FeatureFlaggingService} to use instead of the default one.
   *
   * @param featureFlaggingService
   * @since 4.9, 4.8.2
   */
  void setFeatureFlaggingService(Optional<FeatureFlaggingService> featureFlaggingService);

}
