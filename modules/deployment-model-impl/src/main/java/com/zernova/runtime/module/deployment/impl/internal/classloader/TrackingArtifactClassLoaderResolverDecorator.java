/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.classloader;

import static java.util.Objects.requireNonNull;

import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginDescriptorResolver;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderManager;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;

import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

/**
 * Tracks {@link ArtifactClassLoader}s created by {@link ArtifactClassLoaderResolver}.
 *
 * @since 4.5
 */
public class TrackingArtifactClassLoaderResolverDecorator implements ArtifactClassLoaderResolver {

  private final ArtifactClassLoaderManager artifactClassLoaderManager;
  private final ArtifactClassLoaderResolver delegate;

  /**
   * Tracks the class loaders created by {@link ArtifactClassLoaderResolver}.
   *
   * @param artifactClassLoaderManager tracks each created class loader. Non-null.
   * @param delegate                   resolver that creates the class loaders to be tracked. Non-null.
   */
  public TrackingArtifactClassLoaderResolverDecorator(ArtifactClassLoaderManager artifactClassLoaderManager,
                                                      ArtifactClassLoaderResolver delegate) {
    this.artifactClassLoaderManager = requireNonNull(artifactClassLoaderManager, "artifactClassLoaderManager");
    this.delegate = requireNonNull(delegate, "delegate");
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createDomainClassLoader(DomainDescriptor descriptor) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader = delegate.createDomainClassLoader(descriptor);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createDomainClassLoader(DomainDescriptor descriptor,
                                                                   PluginClassLoaderResolver pluginClassLoaderResolver) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader =
        delegate.createDomainClassLoader(descriptor, pluginClassLoaderResolver);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createDomainClassLoader(DomainDescriptor descriptor,
                                                                   PluginClassLoaderResolver pluginClassLoaderResolver,
                                                                   List<URL> additionalClassloaderUrls) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader =
        delegate.createDomainClassLoader(descriptor, pluginClassLoaderResolver, additionalClassloaderUrls);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader = delegate.createApplicationClassLoader(descriptor);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader =
        delegate.createApplicationClassLoader(descriptor, pluginClassLoaderResolver);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        Supplier<ArtifactClassLoader> domainClassLoader) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader = delegate.createApplicationClassLoader(descriptor, domainClassLoader);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        Supplier<ArtifactClassLoader> domainClassLoader,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver) {
    ZernovaDeployableArtifactClassLoader artifactClassLoader =
        delegate.createApplicationClassLoader(descriptor, domainClassLoader, pluginClassLoaderResolver);
    trackDeployableArtifactClassLoader(artifactClassLoader);
    return artifactClassLoader;
  }

  @Override
  public ZernovaArtifactClassLoader createMulePluginClassLoader(ZernovaDeployableArtifactClassLoader ownerArtifactClassLoader,
                                                             ArtifactPluginDescriptor descriptor,
                                                             PluginDescriptorResolver pluginDescriptorResolver) {
    ZernovaArtifactClassLoader mulePluginClassLoader = delegate
        .createMulePluginClassLoader(ownerArtifactClassLoader, descriptor, pluginDescriptorResolver);
    track(mulePluginClassLoader);
    return mulePluginClassLoader;
  }

  @Override
  public ZernovaArtifactClassLoader createMulePluginClassLoader(ZernovaDeployableArtifactClassLoader ownerArtifactClassLoader,
                                                             ArtifactPluginDescriptor descriptor,
                                                             PluginDescriptorResolver pluginDescriptorResolver,
                                                             PluginClassLoaderResolver pluginClassLoaderResolver) {
    ZernovaArtifactClassLoader mulePluginClassLoader = delegate
        .createMulePluginClassLoader(ownerArtifactClassLoader, descriptor, pluginDescriptorResolver, pluginClassLoaderResolver);
    track(mulePluginClassLoader);
    return mulePluginClassLoader;
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver,
                                                                        List<URL> additionalClassloaderUrls) {
    throw new UnsupportedOperationException("Mule Server does not support loading additional classpath entries.");
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        Supplier<ArtifactClassLoader> domainClassLoader,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver,
                                                                        List<URL> additionalClassloaderUrls) {
    throw new UnsupportedOperationException("Mule Server does not support loading additional classpath entries.");
  }

  private void trackDeployableArtifactClassLoader(ZernovaDeployableArtifactClassLoader artifactClassLoader) {
    artifactClassLoader.getArtifactPluginClassLoaders().forEach(this::track);
    track(artifactClassLoader);
  }

  private void track(ArtifactClassLoader artifactClassLoader) {
    artifactClassLoaderManager.register(artifactClassLoader);
    artifactClassLoader.addShutdownListener(() -> artifactClassLoaderManager.unregister(artifactClassLoader.getArtifactId()));
  }

}
