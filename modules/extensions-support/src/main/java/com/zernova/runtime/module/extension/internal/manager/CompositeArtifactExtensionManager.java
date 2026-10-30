/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager;

import static java.lang.String.format;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composes {@link ExtensionManager} from a child and a parent artifacts, so child artifact can access extensions provided by the
 * parent.
 */
public class CompositeArtifactExtensionManager implements ExtensionManager, Lifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompositeArtifactExtensionManager.class);

  private final ExtensionManager parentExtensionManager;
  private final ExtensionManager childExtensionManager;
  private final Set<ExtensionModel> extensionModels;

  /**
   * Creates a composed extension manager
   *
   * @param parentExtensionManager extension manager for the parent artifact. Non null
   * @param childExtensionManager  extension manager for the child artifact. Non null
   */
  public CompositeArtifactExtensionManager(ExtensionManager parentExtensionManager,
                                           ExtensionManager childExtensionManager) {
    checkArgument(parentExtensionManager != null, "parentExtensionManager cannot be null");
    checkArgument(childExtensionManager != null, "childExtensionManager cannot be null");

    this.parentExtensionManager = parentExtensionManager;
    this.childExtensionManager = childExtensionManager;

    extensionModels = new HashSet<>();
    extensionModels.addAll(parentExtensionManager.getExtensions());
    extensionModels.addAll(childExtensionManager.getExtensions());
  }

  @Override
  public void registerExtension(ExtensionModel extensionModel) {
    throw new UnsupportedOperationException("Composite extension manager cannot register extensions");
  }

  @Override
  public Set<ExtensionModel> getExtensions() {
    return extensionModels;
  }

  @Override
  public Optional<ExtensionModel> getExtension(String extensionName) {
    return extensionModels.stream().filter(extensionModel -> extensionModel.getName().equals(extensionName)).findFirst();
  }

  @Override
  public ConfigurationInstance getConfiguration(String configurationProviderName, CoreEvent event) {
    return getConfigurationProvider(configurationProviderName).map(provider -> provider.get(event))
        .orElseThrow(() -> new IllegalArgumentException(
                                                        format(
                                                               "There is no registered configurationProvider under name '%s'",
                                                               configurationProviderName)));
  }

  @Override
  public Optional<ConfigurationInstance> getConfiguration(ExtensionModel extensionModel,
                                                          ComponentModel componentModel,
                                                          CoreEvent event) {

    Optional<ConfigurationProvider> configurationProvider = getConfigurationProvider(extensionModel, componentModel, event);
    if (configurationProvider.isPresent()) {
      return configurationProvider.map(p -> p.get(event));
    }

    throw new IllegalArgumentException(format(
                                              "There is no registered configuration provider for extension '%s'",
                                              extensionModel.getName()));
  }


  @Override
  public Optional<ConfigurationProvider> getConfigurationProvider(ExtensionModel extensionModel,
                                                                  ComponentModel componentModel,
                                                                  CoreEvent event) {
    Optional<ConfigurationProvider> configuration = childExtensionManager.getConfigurationProvider(extensionModel,
                                                                                                   componentModel,
                                                                                                   event);

    if (configuration.isPresent()) {
      return configuration;
    } else {
      return getConfigurationProvider(extensionModel, componentModel);
    }
  }

  @Override
  public Optional<ConfigurationProvider> getConfigurationProvider(String configurationProviderName) {
    Optional<ConfigurationProvider> configurationProvider =
        childExtensionManager.getConfigurationProvider(configurationProviderName);

    if (!configurationProvider.isPresent()) {
      configurationProvider = parentExtensionManager.getConfigurationProvider(configurationProviderName);
    }

    return configurationProvider;
  }

  public Optional<ConfigurationProvider> getConfigurationProvider(ExtensionModel extensionModel, ComponentModel componentModel) {
    Optional<ConfigurationProvider> configurationModel =
        childExtensionManager.getConfigurationProvider(extensionModel, componentModel);

    if (!configurationModel.isPresent()) {
      configurationModel =
          parentExtensionManager.getConfigurationProvider(extensionModel, componentModel);;
    }

    return configurationModel;
  }

  @Override
  public void registerConfigurationProvider(ConfigurationProvider configurationProvider) {
    throw new UnsupportedOperationException("Composite extension manager cannot register extension providers");
  }

  @Override
  public void unregisterConfigurationProvider(ConfigurationProvider configurationProvider) {
    throw new UnsupportedOperationException("Composite extension manager cannot unregister extension providers");
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(childExtensionManager);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(childExtensionManager);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(childExtensionManager);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(childExtensionManager, LOGGER);
  }

  @Override
  public void disposeConfiguration(String key, ConfigurationInstance configuration) {
    parentExtensionManager.disposeConfiguration(key, configuration);
    childExtensionManager.disposeConfiguration(key, configuration);
  }

  public ExtensionManager getParentExtensionManager() {
    return parentExtensionManager;
  }

  public ExtensionManager getChildExtensionManager() {
    return childExtensionManager;
  }
}
