/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.classloader.container;

import static com.zernova.runtime.container.internal.ContainerClassLoaderCreatorUtils.getLookupPolicy;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;

import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.container.api.discoverer.ContainerDiscovererHelper;
import com.zernova.runtime.container.api.discoverer.ModuleDiscoverer;
import com.zernova.runtime.container.internal.ClasspathModuleDiscoverer;
import com.zernova.runtime.container.internal.ContainerModuleDiscoverer;
import com.zernova.runtime.container.internal.DefaultModuleRepository;
import com.zernova.runtime.container.internal.PreFilteredContainerClassLoaderCreator;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link PreFilteredContainerClassLoaderCreator} useful for functional tests.
 *
 * @since 4.5
 */
public class TestPreFilteredContainerClassLoaderCreator implements PreFilteredContainerClassLoaderCreator {

  private static final String TEST_MODULE_PROPERTIES = "META-INF/mule-test-module.properties";

  private final Set<String> bootPackages;
  private final ClassLoader containerSystemClassloader;
  private final DefaultModuleRepository testContainerModuleRepository;

  public TestPreFilteredContainerClassLoaderCreator(ClassLoader containerSystemClassloader, Set<String> bootPackages,
                                                    Set<String> extraPrivilegedArtifacts) {
    this.containerSystemClassloader = containerSystemClassloader;
    this.bootPackages = bootPackages;

    ModuleDiscoverer moduleDiscoverer = createContainerModuleDiscoverer(containerSystemClassloader);
    this.testContainerModuleRepository =
        new DefaultModuleRepository(new TestModuleDiscoverer(extraPrivilegedArtifacts,
                                                             moduleDiscoverer));
  }

  @Override
  public List<ZernovaContainerModule> getMuleModules() {
    return withContextClassLoader(containerSystemClassloader, testContainerModuleRepository::getModules);
  }

  public ModuleRepository getModuleRepository() {
    return testContainerModuleRepository;
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
  public ArtifactClassLoader getPreFilteredContainerClassLoader(ArtifactDescriptor artifactDescriptor,
                                                                ClassLoader parentClassLoader) {
    return new ZernovaArtifactClassLoader("container", artifactDescriptor,
                                       new URL[0],
                                       parentClassLoader,
                                       getLookupPolicy(parentClassLoader, getMuleModules(),
                                                       getBootPackages()));
  }

  // This reflection adapter layer is required because this class runs from the surefire launcher classloader (java app
  // classloader), while the code in the Discoverer classes has to run within the Mule Container classloader.
  private static ReflectionAdapterModuleDiscoverer createContainerModuleDiscoverer(ClassLoader containerSystemClassloader) {
    try {
      final Class<?> clsContainerDiscovererHelper =
          containerSystemClassloader.loadClass(ContainerDiscovererHelper.class.getName());
      clsContainerDiscovererHelper.getDeclaredMethod("exportInternalsToTestRunner").invoke(null);

      final Class<?> clsContainerModuleDiscoverer =
          containerSystemClassloader.loadClass(ContainerModuleDiscoverer.class.getName());

      final Object moduleDiscoverer = clsContainerModuleDiscoverer
          .getConstructor()
          .newInstance();

      final Class<?> clsClasspathModuleDiscoverer =
          containerSystemClassloader.loadClass(ClasspathModuleDiscoverer.class.getName());
      final Object classpathModuleDiscoverer = clsClasspathModuleDiscoverer
          .getConstructor(String.class)
          .newInstance(TEST_MODULE_PROPERTIES);

      final Class<?> clsModuleDiscoverer =
          containerSystemClassloader.loadClass(ModuleDiscoverer.class.getName());
      clsContainerModuleDiscoverer.getDeclaredMethod("addModuleDiscoverer", clsModuleDiscoverer)
          .invoke(moduleDiscoverer, classpathModuleDiscoverer);

      return new ReflectionAdapterModuleDiscoverer(containerSystemClassloader, moduleDiscoverer);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  private static final class ReflectionAdapterModuleDiscoverer implements ModuleDiscoverer {

    private final ClassLoader containerSystemClassloader;
    private final Object moduleDiscoverer;
    private final Method methodDiscover;

    public ReflectionAdapterModuleDiscoverer(ClassLoader containerSystemClassloader, Object moduleDiscoverer) {
      this.containerSystemClassloader = containerSystemClassloader;
      this.moduleDiscoverer = moduleDiscoverer;

      try {
        methodDiscover = containerSystemClassloader.loadClass(ModuleDiscoverer.class.getName())
            .getDeclaredMethod("discover");
      } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public List<ZernovaContainerModule> discover() {
      try {
        return ((List<?>) methodDiscover.invoke(moduleDiscoverer))
            .stream()
            .map(module -> new ReflectionAdapterZernovaContainerModule(containerSystemClassloader, module))
            .collect(toList());
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new ZernovaRuntimeException(e);
      }

    }

  }

  public static final class ReflectionAdapterZernovaContainerModule implements ZernovaContainerModule {

    private final Object adaptedModule;

    private final Method getName;
    private final Method getExportedPackages;
    private final Method getExportedPaths;
    private final Method getPrivilegedExportedPackages;
    private final Method getPrivilegedArtifacts;

    public ReflectionAdapterZernovaContainerModule(ClassLoader containerSystemClassloader, Object adaptedModule) {
      this.adaptedModule = adaptedModule;

      try {
        final Class<?> clsMuleContainerModule = containerSystemClassloader.loadClass(ZernovaContainerModule.class.getName());

        getName = clsMuleContainerModule.getMethod("getName");
        getExportedPackages = clsMuleContainerModule.getMethod("getExportedPackages");
        getExportedPaths = clsMuleContainerModule.getMethod("getExportedPaths");
        getPrivilegedExportedPackages = clsMuleContainerModule.getMethod("getPrivilegedExportedPackages");
        getPrivilegedArtifacts = clsMuleContainerModule.getMethod("getPrivilegedArtifacts");
      } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public String getName() {
      try {
        return (String) getName.invoke(adaptedModule);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public Set<String> getExportedPackages() {
      try {
        return (Set<String>) getExportedPackages.invoke(adaptedModule);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public Set<String> getExportedPaths() {
      try {
        return (Set<String>) getExportedPaths.invoke(adaptedModule);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public Set<String> getPrivilegedExportedPackages() {
      try {
        return (Set<String>) getPrivilegedExportedPackages.invoke(adaptedModule);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public Set<String> getPrivilegedArtifacts() {
      try {
        return (Set<String>) getPrivilegedArtifacts.invoke(adaptedModule);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    @Override
    public String toString() {
      return "ReflectionAdapterMuleContainerModule_" + adaptedModule.toString();
    }
  }
}
