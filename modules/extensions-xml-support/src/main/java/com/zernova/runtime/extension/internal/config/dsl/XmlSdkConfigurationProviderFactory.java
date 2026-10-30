/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.config.dsl;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.exception.RequiredParameterNotSetException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class XmlSdkConfigurationProviderFactory extends AbstractExtensionObjectFactory<ConfigurationProvider> {

  private final ExtensionModel extensionModel;
  private final ConfigurationModel configurationModel;
  private final Registry registry;

  private final LazyValue<String> configName = new LazyValue<>(this::getName);

  public XmlSdkConfigurationProviderFactory(ExtensionModel extensionModel,
                                            ConfigurationModel configurationModel,
                                            ZernovaContext muleContext,
                                            Registry registry) {
    super(muleContext);
    this.extensionModel = extensionModel;
    this.configurationModel = configurationModel;
    this.registry = registry;
  }

  @Override
  public ConfigurationProvider doGetObject() throws Exception {
    Map<String, String> rawParams = new HashMap<>();
    final Set<Entry<String, Object>> entrySet = getParameters().entrySet();
    for (Entry<String, Object> entry : entrySet) {
      rawParams.put(entry.getKey(), entry.getValue().toString());
    }

    return new XmlSdkConfigurationProvider(configName.get(), rawParams, extensionModel, configurationModel, registry);
  }

  public String getName() {
    return configurationModel.getAllParameterModels().stream()
        .filter(ParameterModel::isComponentId)
        .findAny()
        .map(p -> parameters.get(p.getName()).toString())
        .orElseThrow(() -> new RequiredParameterNotSetException("cannot create a configuration without a name"));
  }

}
