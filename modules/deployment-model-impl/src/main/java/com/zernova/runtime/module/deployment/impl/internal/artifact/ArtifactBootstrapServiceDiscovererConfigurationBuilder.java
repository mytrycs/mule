/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.artifact;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.config.bootstrap.RegistryBootstrapDiscoverer.BOOTSTRAP_PROPERTIES;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapService;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.config.bootstrap.PropertiesBootstrapService;
import com.zernova.runtime.core.api.config.bootstrap.PropertiesBootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.util.PropertiesUtils;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures a {@link BootstrapServiceDiscoverer} on an artifact's {@link ZernovaContext}
 * <p/>
 * Configuration done by this builder will use a {@link BootstrapServiceDiscoverer} to find {@link BootstrapService} provided by
 * the container and will create a {@link BootstrapServiceDiscoverer} for each plugin deployed in the artifact. All the discovered
 * services will then used to configure the context.
 */
public class ArtifactBootstrapServiceDiscovererConfigurationBuilder extends AbstractConfigurationBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactBootstrapServiceDiscovererConfigurationBuilder.class);

  private final List<ArtifactPlugin> artifactPlugins;

  /**
   * Creates a new context builder
   *
   * @param artifactPlugins artifact plugins deployed inside an artifact. Non null.
   */
  public ArtifactBootstrapServiceDiscovererConfigurationBuilder(List<ArtifactPlugin> artifactPlugins) {
    checkArgument(artifactPlugins != null, "ArtifactPlugins cannot be null");
    this.artifactPlugins = artifactPlugins;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    final PropertiesBootstrapServiceDiscoverer propertiesBootstrapServiceDiscoverer =
        new PropertiesBootstrapServiceDiscoverer(this.getClass().getClassLoader());

    List<BootstrapService> bootstrapServices = new LinkedList<>();
    bootstrapServices.addAll(propertiesBootstrapServiceDiscoverer.discover());

    for (ArtifactPlugin artifactPlugin : artifactPlugins) {
      final Enumeration<URL> resources =
          artifactPlugin.getArtifactClassLoader().findResources(BOOTSTRAP_PROPERTIES);

      while (resources.hasMoreElements()) {
        final URL localResource = resources.nextElement();
        LOGGER.debug("Creating PropertiesBootstrapService from properties file: {}", localResource.toString());

        final Properties properties = PropertiesUtils.loadProperties(localResource);
        final BootstrapService pluginBootstrapService =
            new PropertiesBootstrapService(artifactPlugin.getArtifactClassLoader().getClassLoader(), properties);

        bootstrapServices.add(pluginBootstrapService);
      }
    }

    muleContext.setBootstrapServiceDiscoverer(() -> bootstrapServices);
  }
}
