/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static com.zernova.runtime.container.internal.ContainerClassLoaderCreatorUtils.getLookupPolicy;

import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link PreFilteredContainerClassLoaderCreator}.
 *
 * @since 4.5
 */
public class DefaultPreFilteredContainerClassLoaderCreator implements PreFilteredContainerClassLoaderCreator {

  private final ModuleRepository moduleRepository;
  private Set<String> bootPackages = emptySet();
  private Set<String> resourceDirs = emptySet();

  public DefaultPreFilteredContainerClassLoaderCreator(ModuleRepository moduleRepository) {
    this.moduleRepository = requireNonNull(moduleRepository, "moduleRepository cannot be null");
  }

  public DefaultPreFilteredContainerClassLoaderCreator(ModuleRepository moduleRepository, Set<String> bootPackages,
                                                       Set<String> additionalExportedResourceDirectories) {
    this(moduleRepository);
    this.bootPackages = bootPackages;
    this.resourceDirs = additionalExportedResourceDirectories;
  }

  @Override
  public List<ZernovaContainerModule> getMuleModules() {
    return moduleRepository.getModules();
  }

  @Override
  public Set<String> getBootPackages() {
    if (bootPackages.isEmpty()) {
      return BOOT_PACKAGES;
    }
    Set<String> finalBootPackages = new HashSet<>();
    finalBootPackages.addAll(BOOT_PACKAGES);
    finalBootPackages.addAll(bootPackages);
    return finalBootPackages;
  }

  @Override
  public Set<String> getAdditionalExportedResourceDirectories() {
    return resourceDirs;
  }

  @Override
  public ArtifactClassLoader getPreFilteredContainerClassLoader(ArtifactDescriptor artifactDescriptor,
                                                                ClassLoader parentClassLoader) {
    return new ZernovaContainerClassLoader(artifactDescriptor, new URL[0], parentClassLoader,
                                        getLookupPolicy(parentClassLoader, getMuleModules(), getBootPackages()));
  }

  private static final class ZernovaContainerClassLoader extends ZernovaArtifactClassLoader {

    static {
      registerAsParallelCapable();
    }

    private ZernovaContainerClassLoader(ArtifactDescriptor artifactDescriptor, URL[] urls, ClassLoader parent,
                                     ClassLoaderLookupPolicy lookupPolicy) {
      super("container", artifactDescriptor, urls, parent, lookupPolicy);
    }

    @Override
    public URL findResource(String name) {
      // Container classLoader is just an adapter, it does not owns any resource
      return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
      // Container classLoader is just an adapter, it does not owns any resource
      return emptyEnumeration();
    }
  }
}
