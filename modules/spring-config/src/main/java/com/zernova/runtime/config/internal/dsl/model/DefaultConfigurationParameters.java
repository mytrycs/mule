/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.model;

import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.util.MultiMap;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.config.api.dsl.model.ConfigurationParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultConfigurationParameters implements ConfigurationParameters {

  private final Map<String, Object> simpleConfigurationParameters;
  private final MultiMap<ComponentIdentifier, ConfigurationParameters> complexConfigurationParameters;

  private DefaultConfigurationParameters(Map<String, Object> simpleConfigurationParameters,
                                         MultiMap<ComponentIdentifier, ConfigurationParameters> complexConfigurationParameters) {
    this.simpleConfigurationParameters = simpleConfigurationParameters;
    this.complexConfigurationParameters = complexConfigurationParameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String getStringParameter(String parameterName) {
    Object parameter = simpleConfigurationParameters.get(parameterName);
    return parameter != null ? parameter.toString() : null;
  }

  @Override
  public List<ConfigurationParameters> getComplexConfigurationParameter(ComponentIdentifier componentIdentifier) {
    return complexConfigurationParameters.getAll(componentIdentifier);
  }

  @Override
  public List<Pair<ComponentIdentifier, ConfigurationParameters>> getComplexConfigurationParameters() {
    return complexConfigurationParameters.entryList().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
        .collect(toList());
  }

  public static class Builder {

    private final Map<String, Object> simpleConfigurationParameters = new HashMap<>();
    private final MultiMap<ComponentIdentifier, ConfigurationParameters> complexConfigurationParameters = new MultiMap<>();

    private Builder() {}

    public Builder withSimpleParameter(String parameterName, Object parameterValue) {
      this.simpleConfigurationParameters.put(parameterName, parameterValue);
      return this;
    }

    public Builder withComplexParameter(ComponentIdentifier componentIdentifier,
                                        ConfigurationParameters configurationParameters) {
      this.complexConfigurationParameters.put(componentIdentifier, configurationParameters);
      return this;
    }

    public ConfigurationParameters build() {
      return new DefaultConfigurationParameters(simpleConfigurationParameters, complexConfigurationParameters);
    }

  }

}
