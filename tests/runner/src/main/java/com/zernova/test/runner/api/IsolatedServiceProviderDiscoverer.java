/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.api;

import static com.zernova.runtime.core.api.util.ClassUtils.loadClass;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;

import static java.util.Objects.requireNonNull;

import com.zernova.runtime.api.deployment.meta.ZernovaServiceContractModel;
import com.zernova.runtime.api.service.ServiceProvider;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.service.api.discoverer.ImmutableServiceAssembly;
import com.zernova.runtime.module.service.api.discoverer.ZernovaServiceModelLoader;
import com.zernova.runtime.module.service.api.discoverer.ServiceAssembly;
import com.zernova.runtime.module.service.api.discoverer.ServiceProviderDiscoverer;
import com.zernova.runtime.module.service.api.discoverer.ServiceResolutionError;

import java.util.LinkedList;
import java.util.List;

/**
 * Discovers services artifacts using the {@link ArtifactClassLoader} already created.
 *
 * @since 4.0
 */
public class IsolatedServiceProviderDiscoverer implements ServiceProviderDiscoverer {

  private final List<ArtifactClassLoader> serviceArtifactClassLoaders;

  /**
   * Creates a new instance.
   *
   * @param serviceArtifactClassLoaders {@link List} of {@link ArtifactClassLoader}s created for services discovered during
   *                                    classification process. The {@code artifactName} of each {@link ArtifactClassLoader}
   *                                    represents the {@value AetherClassPathClassifier#SERVICE_PROVIDER_CLASS_NAME} defined by
   *                                    the service in its {@value AetherClassPathClassifier##SERVICE_PROPERTIES_FILE_NAME}and it
   *                                    is used for instantiating the {@link ServiceProvider}.
   */
  public IsolatedServiceProviderDiscoverer(final List<ArtifactClassLoader> serviceArtifactClassLoaders) {
    requireNonNull(serviceArtifactClassLoaders, "serviceArtifactClassLoaders cannot be null");
    this.serviceArtifactClassLoaders = serviceArtifactClassLoaders;
  }

  @Override
  public List<ServiceAssembly> discover() throws ServiceResolutionError {
    List<ServiceAssembly> locators = new LinkedList<>();
    for (Object serviceArtifactClassLoader : serviceArtifactClassLoaders) {
      try {
        Object serviceDescriptor = serviceArtifactClassLoader.getClass()
            .getMethod("getArtifactDescriptor").invoke(serviceArtifactClassLoader);
        ClassLoader classLoader = (ClassLoader) serviceArtifactClassLoader.getClass()
            .getMethod("getClassLoader").invoke(serviceArtifactClassLoader);

        List<ZernovaServiceContractModel> contractModels = (List<ZernovaServiceContractModel>) serviceDescriptor.getClass()
            .getMethod("getContractModels").invoke(serviceDescriptor);
        String name = (String) serviceDescriptor.getClass()
            .getMethod("getName").invoke(serviceDescriptor);

        for (Object contract : contractModels) {
          String serviceProviderClassName = (String) contract.getClass()
              .getMethod("getServiceProviderClassName").invoke(contract);
          String contractClassName = (String) contract.getClass()
              .getMethod("getContractClassName").invoke(contract);

          ServiceProvider serviceProvider = instantiateServiceProvider(classLoader, serviceProviderClassName);
          locators.add(new ImmutableServiceAssembly(name, serviceProvider, classLoader,
                                                    loadClass(contractClassName, getClass().getClassLoader())));
        }
      } catch (Exception e) {
        throw new IllegalStateException("Couldn't discover service from class loader: " + serviceArtifactClassLoader, e);
      }
    }

    return locators;
  }

  private ServiceProvider instantiateServiceProvider(ClassLoader classLoader, String className) throws ServiceResolutionError {
    return (ServiceProvider) withContextClassLoader(classLoader,
                                                    () -> ZernovaServiceModelLoader.doInstantiateServiceProvider(className));
  }

}
