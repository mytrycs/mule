/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import static com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProviderLoaderUtils.discoverRuntimeExtensionModels;
import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.boot.ExtensionLoaderUtils.isParallelExtensionModelLoadingEnabled;

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionDiscoveryRequest;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelDiscoverer;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginClassLoaderSupplier;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.extension.api.manager.ExtensionManagerFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Creates {@link ExtensionManager} for mule artifacts that own a {@link ZernovaContext}
 */
public class ArtifactExtensionManagerFactory implements ExtensionManagerFactory {

  private static final BiFunction<PluginClassLoaderSupplier, ExtensionModelLoaderRepository, ExtensionModelDiscoverer> EXT_MODEL_DISCOVERER =
      ExtensionModelDiscoverer::defaultExtensionModelDiscoverer;

  private final Set<ArtifactPluginDescriptor> artifactPluginsDescriptors;
  private final ExtensionManagerFactory extensionManagerFactory;
  private final ExtensionModelDiscoverer extensionModelDiscoverer;

  /**
   * Creates a extensionManager factory
   *
   * @param artifactPlugins                artifact plugins deployed inside the artifact. Non null.
   * @param extensionModelLoaderRepository {@link ExtensionModelLoaderRepository} with the available extension loaders. Non null.
   * @param extensionManagerFactory        creates the {@link ExtensionManager} for the artifact. Non null
   */
  public ArtifactExtensionManagerFactory(List<ArtifactPlugin> artifactPlugins,
                                         ExtensionModelLoaderRepository extensionModelLoaderRepository,
                                         ExtensionManagerFactory extensionManagerFactory) {
    this(artifactPlugins, extensionModelLoaderRepository, extensionManagerFactory, empty());
  }

  /**
   * Creates a extensionManager factory
   *
   * @param artifactPlugins                artifact plugins deployed inside the artifact. Non null.
   * @param extensionModelLoaderRepository {@link ExtensionModelLoaderRepository} with the available extension loaders. Non null.
   * @param extensionManagerFactory        creates the {@link ExtensionManager} for the artifact. Non null
   * @param extModelDiscovererOverride     overrides how the the extension models for plugins in a class loader are calculated.
   */
  public ArtifactExtensionManagerFactory(List<ArtifactPlugin> artifactPlugins,
                                         ExtensionModelLoaderRepository extensionModelLoaderRepository,
                                         ExtensionManagerFactory extensionManagerFactory,
                                         Optional<BiFunction<PluginClassLoaderSupplier, ExtensionModelLoaderRepository, ExtensionModelDiscoverer>> extModelDiscovererOverride) {
    this.extensionManagerFactory = extensionManagerFactory;
    Map<ArtifactPluginDescriptor, ArtifactClassLoader> artifactPluginsClassLoaders = artifactPlugins
        .stream()
        .collect(toMap(ArtifactPlugin::getDescriptor, ArtifactPlugin::getArtifactClassLoader,
                       (x, y) -> y, LinkedHashMap::new));
    this.artifactPluginsDescriptors = artifactPluginsClassLoaders.keySet();
    this.extensionModelDiscoverer =
        extModelDiscovererOverride.orElse(EXT_MODEL_DISCOVERER).apply(artifactPluginsClassLoaders::get,
                                                                      extensionModelLoaderRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExtensionManager create(ZernovaContext muleContext) {
    return create(muleContext, emptySet());
  }

  protected ExtensionManager create(ZernovaContext muleContext, Set<ExtensionModel> parentArtifactExtensions) {
    final ExtensionManager extensionManager = extensionManagerFactory.create(muleContext);
    final Set<ExtensionModel> extensions = new HashSet<>();
    discoverRuntimeExtensionModels()
        .forEach(extensionManager::registerExtension);
    extensions.addAll(extensionModelDiscoverer
        .discoverPluginsExtensionModels(
                                        ExtensionDiscoveryRequest.builder()
                                            .setArtifactPlugins(artifactPluginsDescriptors)
                                            .setParallelDiscovery(isParallelExtensionModelLoadingEnabled())
                                            .setParentArtifactExtensions(parentArtifactExtensions)
                                            .build()));
    extensions.forEach(extensionManager::registerExtension);
    return extensionManager;
  }

}
