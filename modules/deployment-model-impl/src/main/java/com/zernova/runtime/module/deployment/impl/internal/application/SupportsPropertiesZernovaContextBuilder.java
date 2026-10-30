/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application;

import static org.apache.commons.beanutils.BeanUtils.setProperty;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.internal.context.DefaultZernovaContextBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Takes Mule artifact descriptor into account when building the context.
 *
 * @since 4.1
 */
abstract class SupportsPropertiesZernovaContextBuilder extends DefaultZernovaContextBuilder {

  private static final String SYS_PROPERTY_PREFIX = "sys.";
  private static final String MULE_PROPERTY_PREFIX = "mule.config.";

  private final Map<String, String> artifactProperties;

  /**
   * Constructs a new {@link ZernovaContext} builder with the given {@code artifactProperties}.
   *
   * @param artifactType       the type of artifact the target {@link ZernovaContext} is for.
   * @param artifactProperties The properties of the artifact.
   */
  protected SupportsPropertiesZernovaContextBuilder(ArtifactType artifactType, Map<String, String> artifactProperties) {
    super(artifactType);
    this.artifactProperties = artifactProperties;
  }

  protected void initializeFromProperties(ZernovaConfiguration configuration) {
    for (Map.Entry<String, String> entry : artifactProperties.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if (key.startsWith(SYS_PROPERTY_PREFIX)) {
        String systemProperty = key.substring(SYS_PROPERTY_PREFIX.length());
        System.setProperty(systemProperty, value);
      } else if (key.startsWith(MULE_PROPERTY_PREFIX)) {
        String configProperty = key.substring(MULE_PROPERTY_PREFIX.length());
        try {
          setProperty(configuration, configProperty, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
          logger.error("Cannot set configuration property", e);
        }
      }
    }
  }

  protected Map<String, String> getArtifactProperties() {
    return artifactProperties;
  }
}
