/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context;

import static com.zernova.runtime.core.api.context.notification.ServerNotificationManager.createDefaultNotificationManager;
import static com.zernova.runtime.core.internal.registry.SimpleRegistry.createFeatureFlaggingService;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.i18n.I18nMessageFactory;
import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.config.bootstrap.PropertiesBootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.context.ZernovaContextBuilder;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.exception.SystemExceptionHandler;
import com.zernova.runtime.core.api.lifecycle.LifecycleManager;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.core.internal.exception.DefaultSystemExceptionStrategy;
import com.zernova.runtime.core.internal.lifecycle.ZernovaContextLifecycleManager;
import com.zernova.runtime.core.internal.registry.SimpleRegistry;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ZernovaContextBuilder} that uses {@link DefaultZernovaContext} as the default {@link ZernovaContext}
 * implementation and builds it with defaults values for {@link ZernovaConfiguration}, {@link LifecycleManager} and
 * {@link ServerNotificationManager}.
 */
public class DefaultZernovaContextBuilder implements ZernovaContextBuilder {

  protected static final Logger logger = LoggerFactory.getLogger(DefaultZernovaContextBuilder.class);
  private final ArtifactType artifactType;

  protected ZernovaConfiguration config;

  private ZernovaContextLifecycleManager lifecycleManager;

  protected ServerNotificationManager notificationManager;

  protected ClassLoader executionClassLoader;

  protected ObjectSerializer objectSerializer;

  private Optional<Properties> deploymentProperties = empty();

  private List<ZernovaContextListener> listeners = new ArrayList<>();

  private ArtifactCoordinates artifactCoordinates;

  private Optional<FeatureFlaggingService> featureFlaggingService;

  /**
   * Creates a new builder
   *
   * @param artifactType type of the artifact that will own the context created with this builder
   */
  public DefaultZernovaContextBuilder(ArtifactType artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ZernovaContext buildMuleContext() {
    logger.debug("Building new DefaultZernovaContext instance with ZernovaContextBuilder: " + this);
    DefaultZernovaContext muleContext = createDefaultMuleContext();
    muleContext.setMuleConfiguration(injectMuleContextIfRequired(getMuleConfiguration(), muleContext));
    muleContext.setNotificationManager(injectMuleContextIfRequired(getNotificationManager(), muleContext));
    muleContext.setLifecycleManager(injectMuleContextIfRequired(getLifecycleManager(), muleContext));
    muleContext.setArtifactType(artifactType);

    configureRegistry(muleContext);

    muleContext.setExceptionListener(createExceptionListener(muleContext));
    muleContext.setExecutionClassLoader(getExecutionClassLoader());
    muleContext
        .setBootstrapServiceDiscoverer(injectMuleContextIfRequired(createBootstrapDiscoverer(), muleContext));
    muleContext.setDeploymentProperties(getDeploymentProperties());
    muleContext.setListeners(listeners);
    getObjectSerializer(muleContext);

    if (artifactCoordinates != null) {
      ((DefaultZernovaConfiguration) muleContext.getConfiguration()).setArtifactCoordinates(artifactCoordinates);
    }

    return muleContext;
  }

  protected void configureRegistry(DefaultZernovaContext muleContext) {
    final SimpleRegistry registry =
        new SimpleRegistry(muleContext, muleContext.getLifecycleInterceptor(),
                           featureFlaggingService == null ? of(createFeatureFlaggingService(muleContext))
                               : featureFlaggingService);
    muleContext.setRegistry(registry);
    muleContext.setInjector(registry);
  }

  private Properties getDeploymentProperties() {
    if (!deploymentProperties.isPresent()) {
      return new Properties();
    }

    return deploymentProperties.get();
  }

  private void getObjectSerializer(DefaultZernovaContext muleContext) {
    if (objectSerializer == null) {
      objectSerializer = new JavaObjectSerializer(muleContext.getExecutionClassLoader());
    }

    muleContext.setObjectSerializer(objectSerializer);
  }

  private SystemExceptionHandler createExceptionListener(DefaultZernovaContext muleContext) {
    SystemExceptionHandler systemExceptionHandler = muleContext.getRegistry().get("_exceptionListenerFactory");
    if (systemExceptionHandler == null) {
      systemExceptionHandler = new DefaultSystemExceptionStrategy();
    }
    return systemExceptionHandler;
  }

  protected DefaultZernovaContext createDefaultMuleContext() {
    return new DefaultZernovaContext();
  }

  @Override
  public void setMuleConfiguration(ZernovaConfiguration config) {
    this.config = config;
  }

  @Override
  public void setNotificationManager(ServerNotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }

  public ZernovaConfiguration getMuleConfiguration() {
    if (config != null) {
      return config;
    } else {
      return createMuleConfiguration();
    }
  }

  @Override
  public void setExecutionClassLoader(ClassLoader executionClassLoader) {
    this.executionClassLoader = executionClassLoader;
  }

  @Override
  public void setObjectSerializer(ObjectSerializer objectSerializer) {
    this.objectSerializer = objectSerializer;
  }

  protected ClassLoader getExecutionClassLoader() {
    if (executionClassLoader != null) {
      return executionClassLoader;
    } else {
      return Thread.currentThread().getContextClassLoader();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setErrorTypeRepository(ErrorTypeRepository errorTypeRepository) {
    // Nothing to do
  }

  private <T> T injectMuleContextIfRequired(T object, ZernovaContext muleContext) {
    if (object instanceof ZernovaContextAware) {
      ((ZernovaContextAware) object).setMuleContext(muleContext);
    }
    return object;
  }

  protected LifecycleManager getLifecycleManager() {
    if (lifecycleManager != null) {
      return lifecycleManager;
    } else {
      return createLifecycleManager();
    }
  }

  @Override
  public void setLifecycleManager(LifecycleManager manager) {
    if (!(manager instanceof ZernovaContextLifecycleManager)) {
      I18nMessage msg =
          I18nMessageFactory.createStaticMessage("lifecycle manager for ZernovaContext must be a ZernovaContextLifecycleManager");
      throw new ZernovaRuntimeException(msg);
    }

    lifecycleManager = (ZernovaContextLifecycleManager) manager;
  }

  protected ServerNotificationManager getNotificationManager() {
    if (notificationManager != null) {
      return notificationManager;
    } else {
      return createNotificationManager();
    }
  }

  private BootstrapServiceDiscoverer createBootstrapDiscoverer() {
    return new PropertiesBootstrapServiceDiscoverer(DefaultZernovaContextBuilder.class.getClassLoader());
  }

  protected DefaultZernovaConfiguration createMuleConfiguration() {
    return new DefaultZernovaConfiguration();
  }

  private ZernovaContextLifecycleManager createLifecycleManager() {
    return new ZernovaContextLifecycleManager();
  }

  protected ServerNotificationManager createNotificationManager() {
    return createDefaultNotificationManager();
  }

  @Override
  public String toString() {
    return ClassUtils.getClassName(getClass()) + "{muleConfiguration=" + config + ", lifecycleManager=" + lifecycleManager
        + ", notificationManager=" + notificationManager + "}";
  }

  @Override
  public void setDeploymentProperties(Optional<Properties> deploymentProperties) {
    this.deploymentProperties = deploymentProperties;
  }

  @Override
  public void setListeners(List<ZernovaContextListener> listeners) {
    this.listeners = listeners;
  }

  @Override
  public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    this.artifactCoordinates = artifactCoordinates;
  }

  @Override
  public void setFeatureFlaggingService(Optional<FeatureFlaggingService> featureFlaggingService) {
    this.featureFlaggingService = featureFlaggingService;
  }
}
