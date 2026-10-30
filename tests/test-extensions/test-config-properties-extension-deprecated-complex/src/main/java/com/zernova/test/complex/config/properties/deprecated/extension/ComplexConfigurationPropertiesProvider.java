/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.complex.config.properties.deprecated.extension;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;

import com.zernova.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import com.zernova.runtime.config.api.dsl.model.properties.ConfigurationProperty;

import java.util.List;
import java.util.Optional;

public class ComplexConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

  private final List<String> textsFromComplexParams;

  public ComplexConfigurationPropertiesProvider(List<String> textsFromComplexParams) {
    this.textsFromComplexParams = textsFromComplexParams;
  }

  @Override
  public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {
    if (textsFromComplexParams.isEmpty()) {
      return empty();
    }

    return of(new ConfigurationProperty() {

      @Override
      public Object getSource() {
        return "ComplexConfigurationPropertiesProvider";
      }

      @Override
      public Object getRawValue() {
        return textsFromComplexParams.stream().collect(joining(","));
      }

      @Override
      public String getKey() {
        return "textsFromComplexParams";
      }
    });
  }

  @Override
  public String getDescription() {
    return "ComplexConfigurationPropertiesProvider";
  }

}
