/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.config.dsl;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import java.util.Map;

/**
 * {@link ConfigurationProvider} implementation for Xml-Sdk connectors.
 *
 * @since 4.3
 */
public class XmlSdkConfigurationProvider extends AbstractComponent implements ConfigurationProvider {

  private final String name;
  private final Map<String, String> parameters;
  private final ExtensionModel extensionModel;
  private final ConfigurationModel configurationModel;
  private final Registry registry;

  public XmlSdkConfigurationProvider(String name,
                                     Map<String, String> parameters,
                                     ExtensionModel extensionModel,
                                     ConfigurationModel configurationModel,
                                     Registry registry) {
    this.name = name;
    this.parameters = parameters;
    this.extensionModel = extensionModel;
    this.configurationModel = configurationModel;
    this.registry = registry;
  }


  public Map<String, String> getParameters() {
    return parameters;
  }

  @Override
  public ConfigurationInstance get(Event event) {
    return new XmlSdkCompositeConfigurationInstance(name, configurationModel, event, registry);
  }

  @Override
  public boolean isDynamic() {
    return false;
  }

  @Override
  public ExtensionModel getExtensionModel() {
    return extensionModel;
  }

  @Override
  public ConfigurationModel getConfigurationModel() {
    return configurationModel;
  }

  @Override
  public String getName() {
    return this.name;
  }

}
