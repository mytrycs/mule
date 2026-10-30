/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.discoverer;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.rx.Exceptions.unwrap;
import static com.zernova.runtime.module.service.api.discoverer.ZernovaServiceModelLoader.instantiateServiceProvider;
import static com.zernova.runtime.module.service.api.discoverer.ServiceAssembly.lazyBuilder;

import static java.security.AccessController.doPrivileged;
import static java.security.AccessController.getContext;
import static java.util.Optional.empty;

import com.zernova.runtime.api.deployment.meta.ZernovaServiceContractModel;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.container.api.ZernovaFoldersUtil;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.exception.ArtifactClassloaderCreationException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.service.api.artifact.ServiceDescriptor;
import com.zernova.runtime.module.service.api.discoverer.ServiceAssembly;
import com.zernova.runtime.module.service.api.discoverer.ServiceProviderDiscoverer;
import com.zernova.runtime.module.service.api.discoverer.ServiceResolutionError;
import com.zernova.runtime.module.service.internal.artifact.ServiceDescriptorFactory;

import java.io.File;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Discovers services artifacts from the {@link ZernovaFoldersUtil#SERVICES_FOLDER} folder.
 */
public class FileSystemServiceProviderDiscoverer implements ServiceProviderDiscoverer {

  private static final AccessControlContext ACCESS_CONTROL_CTX = getContext();

  private final ArtifactClassLoader apiClassLoader;
  private final ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory;
  private final DescriptorLoaderRepository descriptorLoaderRepository;
  private final ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder;
  private final Supplier<File> targetServicesFolder;

  /**
   * Creates a new instance.
   *
   * @param containerClassLoader               container artifact classLoader. Non null.
   * @param serviceClassLoaderFactory          factory used to create service's classloaders. Non null.
   * @param descriptorLoaderRepository         contains all the {@link ClassLoaderConfigurationLoader} registered on the
   *                                           container. Non null
   * @param artifactDescriptorValidatorBuilder {@link ArtifactDescriptorValidatorBuilder} to create the
   *                                           {@link com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator}
   *                                           in order to check the state of the descriptor once loaded.
   * @param targetServicesFolder               {@link File} where services are exploded and would be discovered. Non null.
   */
  public FileSystemServiceProviderDiscoverer(ArtifactClassLoader containerClassLoader,
                                             ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory,
                                             DescriptorLoaderRepository descriptorLoaderRepository,
                                             ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder,
                                             File targetServicesFolder) {
    this(containerClassLoader, serviceClassLoaderFactory, descriptorLoaderRepository, artifactDescriptorValidatorBuilder,
         () -> targetServicesFolder);
  }

  /**
   * Creates a new instance that discover services from Mule Runtime services folder.
   *
   * @param containerClassLoader               container artifact classLoader. Non null.
   * @param serviceClassLoaderFactory          factory used to create service's classloaders. Non null.
   * @param descriptorLoaderRepository         contains all the {@link ClassLoaderConfigurationLoader} registered on the
   *                                           container. Non null
   * @param artifactDescriptorValidatorBuilder {@link ArtifactDescriptorValidatorBuilder} to create the
   *                                           {@link com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator}
   *                                           in order to check the state of the descriptor once loaded.
   */
  public FileSystemServiceProviderDiscoverer(ArtifactClassLoader containerClassLoader,
                                             ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory,
                                             DescriptorLoaderRepository descriptorLoaderRepository,
                                             ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    this(containerClassLoader, serviceClassLoaderFactory, descriptorLoaderRepository, artifactDescriptorValidatorBuilder,
         ZernovaFoldersUtil::getServicesFolder);
  }

  // Supplier in order to resolve the services folder after initialization due use MULE_HOME and tests set
  // this value after these discoverer is created
  private FileSystemServiceProviderDiscoverer(ArtifactClassLoader containerClassLoader,
                                              ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory,
                                              DescriptorLoaderRepository descriptorLoaderRepository,
                                              ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder,
                                              Supplier<File> targetServicesFolder) {
    checkArgument(containerClassLoader != null, "containerClassLoader cannot be null");
    checkArgument(serviceClassLoaderFactory != null, "serviceClassLoaderFactory cannot be null");
    checkArgument(artifactDescriptorValidatorBuilder != null, "artifactDescriptorValidatorBuilder cannot be null");
    checkArgument(targetServicesFolder != null, "targetServicesFolder cannot be null");

    this.descriptorLoaderRepository = descriptorLoaderRepository;
    this.apiClassLoader = containerClassLoader;
    this.serviceClassLoaderFactory = serviceClassLoaderFactory;
    this.artifactDescriptorValidatorBuilder = artifactDescriptorValidatorBuilder;
    this.targetServicesFolder = targetServicesFolder;

  }

  @Override
  public List<ServiceAssembly> discover() throws ServiceResolutionError {
    final ServiceDescriptorFactory serviceDescriptorFactory =
        new ServiceDescriptorFactory(descriptorLoaderRepository, artifactDescriptorValidatorBuilder);

    final List<ServiceDescriptor> serviceDescriptors = getServiceDescriptors(serviceDescriptorFactory);

    return assemble(serviceDescriptors, serviceClassLoaderFactory);
  }

  private List<ServiceDescriptor> getServiceDescriptors(ServiceDescriptorFactory serviceDescriptorFactory)
      throws ServiceResolutionError {

    final File[] serviceDirectories = this.targetServicesFolder.get().listFiles(File::isDirectory);
    List<ServiceDescriptor> foundServices = new ArrayList<>(serviceDirectories.length);

    for (File serviceDirectory : serviceDirectories) {
      try {
        final ServiceDescriptor serviceDescriptor = serviceDescriptorFactory.create(serviceDirectory, empty());
        foundServices.add(serviceDescriptor);
      } catch (Exception e) {
        throw new ServiceResolutionError("Error processing service JAR file", unwrap(e));
      }
    }
    return foundServices;
  }

  private List<ServiceAssembly> assemble(List<ServiceDescriptor> serviceDescriptors,
                                         ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory)
      throws ServiceResolutionError {

    List<ServiceAssembly> assemblies = new ArrayList<>(serviceDescriptors.size());
    for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {

      final Supplier<ClassLoader> serviceClassLoader =
          new LazyValue<>(() -> createClassLoader(serviceClassLoaderFactory, serviceDescriptor));

      for (ZernovaServiceContractModel contract : serviceDescriptor.getContractModels()) {
        try {
          ServiceAssembly assembly = lazyBuilder()
              .withName(serviceDescriptor.getName())
              .withClassLoader(serviceClassLoader)
              .withServiceProvider(() -> instantiateServiceProvider(contract))
              .forContract(contract.getContractClassName())
              .build();
          assemblies.add(assembly);
        } catch (ZernovaRuntimeException e) {
          if (e.getCause() != null) {
            throw new ServiceResolutionError("Error Loading service", e.getCause());
          } else {
            throw new ServiceResolutionError("Error Loading service", e);
          }
        }
      }
    }

    return assemblies;
  }

  private ClassLoader createClassLoader(ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory,
                                        ServiceDescriptor serviceDescriptor) {
    // NOTE: We have to use this doPrivileged() here to set the AccessControlContext, even if it is deprecated. We need to do it
    // because otherwise it will be calculated based on the full stack (see AccessController#getContext), and it causes
    // ZernovaApplicationClassLoader leaks.
    return doPrivileged((PrivilegedAction<ClassLoader>) () -> {
      try {
        return (ClassLoader) serviceClassLoaderFactory
            .create(getServiceArtifactId(serviceDescriptor),
                    serviceDescriptor,
                    apiClassLoader.getClassLoader(),
                    apiClassLoader.getClassLoaderLookupPolicy());
      } catch (ArtifactClassloaderCreationException e) {
        throw new ZernovaRuntimeException(e);
      }
    }, ACCESS_CONTROL_CTX);
  }

  private String getServiceArtifactId(ServiceDescriptor serviceDescriptor) {
    return "service/" + serviceDescriptor.getName();
  }
}
