/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application;

import static com.zernova.runtime.core.api.config.ZernovaDeploymentProperties.MULE_LAZY_INIT_DEPLOYMENT_PROPERTY;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.util.StringUtils.isBlank;

import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.core.api.config.ReconfigurableZernovaConfiguration;

import java.util.Map;

/**
 * Takes Mule application descriptor into account when building the context.
 */
public class ApplicationZernovaContextBuilder extends SupportsPropertiesZernovaContextBuilder {

  private final String appName;
  private final String defaultEncoding;

  public ApplicationZernovaContextBuilder(String appName, Map<String, String> appProperties, String defaultEncoding) {
    super(APP, appProperties);
    this.appName = appName;
    this.defaultEncoding = defaultEncoding;
  }

  @Override
  protected DefaultZernovaConfiguration createMuleConfiguration() {
    final DefaultZernovaConfiguration configuration;
    if (Boolean.valueOf(getArtifactProperties().get(MULE_LAZY_INIT_DEPLOYMENT_PROPERTY))) {
      configuration = new ReconfigurableZernovaConfiguration(true);
    } else {
      configuration = new DefaultZernovaConfiguration(true);
    }

    initializeFromProperties(configuration);
    configuration.setId(appName);
    final String encoding = defaultEncoding;
    if (!isBlank(encoding)) {
      configuration.setDefaultEncoding(encoding);
    }
    if (executionClassLoader instanceof ZernovaArtifactClassLoader) {
      configuration
          .setMinMuleVersion(((ZernovaArtifactClassLoader) executionClassLoader).getArtifactDescriptor().getMinMuleVersion());
    }
    return configuration;
  }
}
