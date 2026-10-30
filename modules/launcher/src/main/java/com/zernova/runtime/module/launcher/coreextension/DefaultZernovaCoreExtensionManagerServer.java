/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider.loadContainerServiceProviders;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.container.api.ArtifactClassLoaderManagerAware;
import com.zernova.runtime.container.api.CoreExtensionsAware;
import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.event.EventContextService;
import com.zernova.runtime.core.internal.lock.ServerLockFactory;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderManager;
import com.zernova.runtime.module.deployment.api.ArtifactDeploymentListener;
import com.zernova.runtime.module.deployment.api.DeploymentListener;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.api.DeploymentServiceAware;
import com.zernova.runtime.module.deployment.internal.DeploymentListenerAdapter;
import com.zernova.runtime.module.repository.api.RepositoryService;
import com.zernova.runtime.module.repository.api.RepositoryServiceAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class DefaultZernovaCoreExtensionManagerServer implements ZernovaCoreExtensionManagerServer {

  private static final Logger LOGGER = getLogger(DefaultZernovaCoreExtensionManagerServer.class);

  private final ZernovaCoreExtensionDiscoverer coreExtensionDiscoverer;
  private final ZernovaCoreExtensionDependencyResolver coreExtensionDependencyResolver;
  private List<ZernovaCoreExtension> coreExtensions = new LinkedList<>();
  private DeploymentService deploymentService;
  private RepositoryService repositoryService;
  private List<ZernovaCoreExtension> orderedCoreExtensions;
  private ArtifactClassLoaderManager artifactClassLoaderManager;
  private ServiceRepository serviceRepository;
  private EventContextService eventContextService;
  private Map<Class, Object> containerServices = new HashMap<>();

  private List<ZernovaCoreExtension> initializedCoreExtensions = new ArrayList<>();
  private List<ZernovaCoreExtension> startedCoreExtensions = new ArrayList<>();
  private ServerLockFactory serverLockFactory;

  public DefaultZernovaCoreExtensionManagerServer(ZernovaCoreExtensionDiscoverer coreExtensionDiscoverer,
                                               ZernovaCoreExtensionDependencyResolver coreExtensionDependencyResolver) {
    this.coreExtensionDiscoverer = coreExtensionDiscoverer;
    this.coreExtensionDependencyResolver = coreExtensionDependencyResolver;
  }

  @Override
  public void dispose() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Disposing core extensions");
    }
    for (ZernovaCoreExtension extension : coreExtensions) {

      if (initializedCoreExtensions.contains(extension)) {
        try {
          extension.dispose();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Core extension '{}' disposed", extension.toString());
          }
        } catch (Exception ex) {
          LOGGER.error("Error disposing core extension " + extension.getName(), ex);
        }
      }
    }
    initializedCoreExtensions.clear();
  }

  @Override
  public void initialise() throws InitialisationException {
    try {
      coreExtensions = coreExtensionDiscoverer.discover();

      orderedCoreExtensions = coreExtensionDependencyResolver.resolveDependencies(coreExtensions);

      initializeCoreExtensions();

    } catch (Exception e) {
      throw new InitialisationException(e, this);
    }
  }

  @Override
  public void start() throws ZernovaException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Starting core extensions");
    }
    for (ZernovaCoreExtension extension : orderedCoreExtensions) {
      extension.start();
      startedCoreExtensions.add(extension);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Core extension '{}' started", extension.toString());
      }
    }
  }

  @Override
  public void stop() throws ZernovaException {
    if (orderedCoreExtensions == null) {
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Stopping core extensions");
    }
    for (int i = orderedCoreExtensions.size() - 1; i >= 0; i--) {
      ZernovaCoreExtension extension = orderedCoreExtensions.get(i);

      if (startedCoreExtensions.contains(extension)) {
        try {
          extension.stop();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Core extension '{}' stopped", extension.toString());
          }
        } catch (Throwable e) {
          LOGGER.warn("Error stopping core extension: " + extension.getName(), e);
        }
      }
    }
    startedCoreExtensions.clear();
  }

  private void initializeCoreExtensions() throws ZernovaException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Initializing core extensions");
    }

    Injector simpleRegistry = createContainerInjector();

    for (ZernovaCoreExtension extension : orderedCoreExtensions) {

      loadContainerServiceProviders()
          .forEach(containerServiceProvider -> containerServiceProvider.inject(extension,
                                                                               containerServices.get(containerServiceProvider
                                                                                   .getServiceInterface())));


      if (extension instanceof DeploymentServiceAware) {
        ((DeploymentServiceAware) extension).setDeploymentService(deploymentService);
      }

      if (extension instanceof RepositoryServiceAware) {
        ((RepositoryServiceAware) extension).setRepositoryService(repositoryService);
      }

      if (extension instanceof ArtifactDeploymentListener) {
        deploymentService.addDeploymentListener(createDeploymentListenerAdapter((ArtifactDeploymentListener) extension, APP));
        deploymentService
            .addDomainDeploymentListener(createDeploymentListenerAdapter((ArtifactDeploymentListener) extension, DOMAIN));
      }

      if (extension instanceof DeploymentListener) {
        deploymentService.addDeploymentListener((DeploymentListener) extension);
      }

      if (extension instanceof CoreExtensionsAware) {
        ((CoreExtensionsAware) extension).setCoreExtensions(orderedCoreExtensions);
      }

      if (extension instanceof ArtifactClassLoaderManagerAware) {
        ((ArtifactClassLoaderManagerAware) extension).setArtifactClassLoaderManager(artifactClassLoaderManager);
      }

      simpleRegistry.inject(extension);

      extension.initialise();
      initializedCoreExtensions.add(extension);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Core extension '{}' initialized", extension.toString());
      }
    }
  }

  private Injector createContainerInjector() {
    final var injectorBuilder = new ContainerInjectorBuilder<>()
        .withDeploymentService(deploymentService)
        .withRepositoryService(repositoryService)
        .withServiceRepository(serviceRepository)
        .withCoreExtensions(coreExtensions)
        .withArtifactClassLoaderManager(artifactClassLoaderManager)
        .withEventContextService(eventContextService)
        .withServerLockFactory(serverLockFactory);
    containerServices.forEach((k, v) -> injectorBuilder.registerObject(k.getName(), v));

    return injectorBuilder.build();
  }

  @Override
  public void setDeploymentService(DeploymentService deploymentService) {
    this.deploymentService = deploymentService;
  }

  @Override
  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  @Override
  public void setArtifactClassLoaderManager(ArtifactClassLoaderManager artifactClassLoaderManager) {
    this.artifactClassLoaderManager = artifactClassLoaderManager;
  }

  @Override
  public void setEventContextService(EventContextService eventContextService) {
    this.eventContextService = eventContextService;
  }

  @Override
  public void setServiceRepository(ServiceRepository serviceRepository) {
    this.serviceRepository = serviceRepository;
  }

  @Override
  public void setServerLockFactory(ServerLockFactory serverLockFactory) {
    this.serverLockFactory = serverLockFactory;
  }

  @Override
  public void setContainerServices(Map<Class, Object> containerServices) {
    this.containerServices = containerServices;
  }

  /**
   * Creates a {@link DeploymentListenerAdapter}.
   *
   * @param artifactDeploymentListener the artifactDeploymentListener to be adapted.
   * @param type:                      the artifact type.
   * @return an DeploymentListener.
   */
  DeploymentListener createDeploymentListenerAdapter(ArtifactDeploymentListener artifactDeploymentListener, ArtifactType type) {
    return new DeploymentListenerAdapter(artifactDeploymentListener, type);
  }
}
