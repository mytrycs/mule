/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.artifact;

import static com.zernova.runtime.api.util.Preconditions.checkNotNull;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.module.extension.api.manager.ExtensionManagerFactory;

import java.util.List;

/**
 * Implementation of {@link ConfigurationBuilder} that registers a {@link ExtensionManager}
 *
 * @since 4.0
 */
public class ArtifactExtensionManagerConfigurationBuilder extends AbstractConfigurationBuilder {

  public static final String META_INF_FOLDER = "META-INF";

  private final List<ArtifactPlugin> artifactPlugins;
  private final ExtensionManagerFactory extensionManagerFactory;

  /**
   * Creates an instance of the configuration builder.
   *
   * @param artifactPlugins         {@link List} of {@link ArtifactPlugin ArtifactPlugins} to be registered.
   * @param extensionManagerFactory creates the extension manager for this artifact. Non null.
   */
  public ArtifactExtensionManagerConfigurationBuilder(List<ArtifactPlugin> artifactPlugins,
                                                      ExtensionManagerFactory extensionManagerFactory) {
    checkNotNull(artifactPlugins, "artifactPlugins cannot be null");
    checkNotNull(extensionManagerFactory, "extensionManagerFactory cannot be null");

    this.artifactPlugins = artifactPlugins;
    this.extensionManagerFactory = extensionManagerFactory;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    ExtensionManager extensionManager = extensionManagerFactory.create(muleContext);

    muleContext.setExtensionManager(extensionManager);
  }
}
