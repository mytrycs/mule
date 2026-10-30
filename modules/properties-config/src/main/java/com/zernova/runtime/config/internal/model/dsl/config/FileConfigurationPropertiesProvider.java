/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.model.dsl.config;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.core.api.exception.ResourceNotFoundException;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.properties.api.ConfigurationPropertiesProvider;
import com.zernova.runtime.properties.api.ConfigurationProperty;
import com.zernova.runtime.properties.api.ResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * File based properties provider, using an external resource provider.
 */
public class FileConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

  private final static String FILE_PREFIX = "file::";
  private final ResourceProvider resourceProvider;
  private final String description;

  public FileConfigurationPropertiesProvider(ResourceProvider resourceProvider, String description) {
    this.resourceProvider = resourceProvider;
    this.description = description;
  }

  @Override
  public Optional<ConfigurationProperty> provide(String configurationAttributeKey) {
    if (configurationAttributeKey.startsWith(FILE_PREFIX)) {
      String path = configurationAttributeKey.substring(FILE_PREFIX.length());
      try (InputStream is = resourceProvider.getResourceAsStream(path)) {
        if (is != null) {
          String value = IOUtils.toString(is);
          return of(new DefaultConfigurationProperty(this, configurationAttributeKey, value));
        }
      } catch (IOException | ResourceNotFoundException e) {
        // ignore close exception
      }
    }

    return empty();
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
