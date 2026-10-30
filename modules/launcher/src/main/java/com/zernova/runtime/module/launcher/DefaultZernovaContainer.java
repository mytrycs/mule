/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.module.launcher;

import static com.zernova.runtime.api.exception.ExceptionHelper.getRootException;
import static com.zernova.runtime.api.exception.ExceptionHelper.getRootMuleException;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_SIMPLE_LOG;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getExecutionFolder;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.fatalErrorInShutdown;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.fatalErrorWhileRunning;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.core.api.util.StringMessageUtils.getBoilerPlate;
import static com.zernova.runtime.module.deployment.internal.DeploymentServiceBuilder.deploymentServiceBuilder;
import static com.zernova.runtime.module.deployment.internal.ZernovaDeploymentService.findSchedulerService;
import static com.zernova.runtime.module.deployment.internal.processor.SerializedAstArtifactConfigurationProcessor.serializedAstWithFallbackArtifactConfigurationProcessor;
import static com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider.loadContainerServiceProviders;
import static com.zernova.runtime.module.log4j.boot.api.ZernovaLog4jContextFactory.createAndInstall;
import static com.zernova.runtime.module.log4j.internal.ZernovaLog4jConfiguratorUtils.configureSelector;
import static com.zernova.runtime.module.log4j.internal.ZernovaLog4jConfiguratorUtils.getDefaultReconfigurationAction;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;

import static org.apache.logging.log4j.LogManager.getFactory;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.lock.ServerLockFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.net.ZernovaArtifactUrlStreamHandler;
import com.zernova.runtime.module.artifact.api.classloader.net.ZernovaUrlStreamHandlerFactory;
import com.zernova.runtime.module.artifact.internal.classloader.DefaultResourceInitializer;
import com.zernova.runtime.module.boot.api.ZernovaContainer;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.impl.internal.ZernovaArtifactResourcesRegistry;
import com.zernova.runtime.module.launcher.coreextension.ClasspathZernovaCoreExtensionDiscoverer;
import com.zernova.runtime.module.launcher.coreextension.DefaultZernovaCoreExtensionManagerServer;
import com.zernova.runtime.module.launcher.coreextension.ZernovaCoreExtensionManagerServer;
import com.zernova.runtime.module.launcher.coreextension.ReflectionZernovaCoreExtensionDependencyResolver;
import com.zernova.runtime.module.launcher.splash.ZernovaContainerStartupSplashScreen;
import com.zernova.runtime.module.log4j.boot.api.ZernovaLog4jContextFactory;
import com.zernova.runtime.module.repository.api.RepositoryService;
import com.zernova.runtime.module.repository.internal.RepositoryServiceFactory;
import com.zernova.runtime.module.service.api.manager.ServiceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultZernovaContainer implements ZernovaContainer {

  /**
   * logger used by this class
   */
  private static final Logger logger;

  private static final Logger SPLASH_LOGGER = LoggerFactory.getLogger("com.zernova.runtime.core.internal.logging");

  /**
   * The Runtime shutdown thread used to undeploy this server
   */
  private static ZernovaShutdownHook muleShutdownHook;

  protected final DeploymentService deploymentService;
  private final RepositoryService repositoryService;
  private final ZernovaCoreExtensionManagerServer coreExtensionManager;
  private ServerLockFactory muleLockFactory;
  private final ZernovaArtifactResourcesRegistry artifactResourcesRegistry = new ZernovaArtifactResourcesRegistry.Builder()
      .artifactConfigurationProcessor(serializedAstWithFallbackArtifactConfigurationProcessor())
      .withActionOnMuleArtifactDeployment(getDefaultReconfigurationAction())
      .withAdditionalResourceDirectory("")
      .build();
  private Map<Class, Object> containerServices = new HashMap<>();

  private static ZernovaLog4jContextFactory log4jContextFactory;

  static {
    if (getProperty(MULE_SIMPLE_LOG) == null) {
      LoggerContextFactory contextFactory = getFactory();
      if (contextFactory instanceof ZernovaLog4jContextFactory) {
        log4jContextFactory = (ZernovaLog4jContextFactory) contextFactory;
      } else {
        log4jContextFactory = createAndInstall();
      }
      configureSelector(log4jContextFactory);
    }

    logger = LoggerFactory.getLogger(DefaultZernovaContainer.class);
  }

  private final ServiceManager serviceManager;
  private final ExtensionModelLoaderRepository extensionModelLoaderRepository;

  public DefaultZernovaContainer() throws InitialisationException {
    init();

    this.serviceManager = artifactResourcesRegistry.getServiceManager();

    this.extensionModelLoaderRepository = artifactResourcesRegistry.getExtensionModelLoaderRepository();

    this.deploymentService = resolveDeploymentService();

    this.repositoryService = new RepositoryServiceFactory().createRepositoryService();

    this.coreExtensionManager =
        new DefaultZernovaCoreExtensionManagerServer(new ClasspathZernovaCoreExtensionDiscoverer(artifactResourcesRegistry
            .getContainerClassLoader()),
                                                  new ReflectionZernovaCoreExtensionDependencyResolver());
    this.muleLockFactory = new ServerLockFactory();

    loadContainerServiceProviders()
        .forEach(containerServiceProvider -> setService(containerServiceProvider.getServiceInterface(),
                                                        containerServiceProvider.getServiceImplementation(deploymentService,
                                                                                                          artifactResourcesRegistry)));

    artifactResourcesRegistry.getContainerClassLoader().dispose();
  }

  private DeploymentService resolveDeploymentService() {
    return deploymentServiceBuilder()
        .withArtifactStartExecutorSupplier(() -> findSchedulerService(serviceManager))
        .withApplicationFactory(artifactResourcesRegistry.getApplicationFactory())
        .withDomainFactory(artifactResourcesRegistry.getDomainFactory())
        .withDeploymentFailureThrowableConsumer(this::attemptContainerShutdown)
        .build();
  }

  /**
   * Configure the server.
   */
  DefaultZernovaContainer(DeploymentService deploymentService, RepositoryService repositoryService,
                       ZernovaCoreExtensionManagerServer coreExtensionManager,
                       ServiceManager serviceManager, ExtensionModelLoaderRepository extensionModelLoaderRepository,
                       Map<Class, Object> containerServices)
      throws IllegalArgumentException, InitialisationException {
    init();

    this.deploymentService = deploymentService;
    this.coreExtensionManager = coreExtensionManager;
    this.repositoryService = repositoryService;
    this.serviceManager = serviceManager;
    this.extensionModelLoaderRepository = extensionModelLoaderRepository;
    this.containerServices = containerServices;
  }

  protected void init() throws IllegalArgumentException, InitialisationException {
    // set our own UrlStreamHandlerFactory to become more independent of system
    // properties
    ZernovaUrlStreamHandlerFactory.installUrlStreamHandlerFactory();
    ZernovaArtifactUrlStreamHandler.register();

    artifactResourcesRegistry.getMemoryManagementService().initialise();
    artifactResourcesRegistry.inject(artifactResourcesRegistry.getContainerProfilingService());
    initialiseIfNeeded(artifactResourcesRegistry.getContainerProfilingService());
  }

  private void createExecutionMuleFolder() {
    File executionFolder = getExecutionFolder();
    if (!executionFolder.exists()) {
      if (!executionFolder.mkdirs()) {
        throw new ZernovaRuntimeException(createStaticMessage(format(
                                                                  "Could not create folder %s, validate that the process has permissions over that directory",
                                                                  executionFolder.getAbsolutePath())));
      }
    }
  }

  @Override
  public void start(Future<Boolean> configurationsReady, List<String> additionalSplashEntries) throws ZernovaException {
    start(true, configurationsReady, additionalSplashEntries);
  }

  public void start(boolean registerShutdownHook) throws ZernovaException {
    start(registerShutdownHook, completedFuture(true), emptyList());
  }

  private void start(boolean registerShutdownHook, Future<Boolean> configurationsReady,
                     List<String> additionalSplashEntries)
      throws ZernovaException {
    if (registerShutdownHook) {
      registerShutdownHook();
    }
    try {
      startIfNeeded(artifactResourcesRegistry.getContainerProfilingService());

      doResourceInitialization();

      createExecutionMuleFolder();

      serviceManager.start();

      coreExtensionManager.setDeploymentService(deploymentService);
      coreExtensionManager.setRepositoryService(repositoryService);
      coreExtensionManager.setArtifactClassLoaderManager(artifactResourcesRegistry.getArtifactClassLoaderManager());
      coreExtensionManager.setServiceRepository(serviceManager);
      coreExtensionManager.setServerLockFactory(muleLockFactory);
      coreExtensionManager.setContainerServices(containerServices);

      // Waits for all bootstrapping configurations to be ready before progressing any further
      if (!configurationsReady.get()) {
        shutdown();
      }

      showSplashScreen(additionalSplashEntries);

      coreExtensionManager.initialise();
      coreExtensionManager.start();
      initialiseIfNeeded(containerServices);

      startIfNeeded(extensionModelLoaderRepository);
      deploymentService.start();
    } catch (ZernovaException e) {
      shutdown(e);
      throw e;
    } catch (Throwable t) {
      shutdown(t);
      throw new ZernovaRuntimeException(t);
    }
  }

  private void attemptContainerShutdown(Throwable e) {
    try {
      shutdown(e);
    } catch (Throwable t) {
      logger.error("Error on attempting to shutdown the container", t);
    }
    exit(1);
  }

  private void doResourceInitialization() {
    withContextClassLoader(getSystemClassLoader(), () -> {
      try {
        new DefaultResourceInitializer().initialize();
      } catch (Exception e) {
        logger.error("Cannot create resource initializer instance", e);
      }
    });
  }

  private void showSplashScreen(List<String> additionalSplashEntries) {
    final ZernovaContainerStartupSplashScreen splashScreen = new ZernovaContainerStartupSplashScreen(additionalSplashEntries);
    splashScreen.doBody();
    SPLASH_LOGGER.info(splashScreen.toString());
  }

  /**
   * Will shut down the server displaying the cause and time of the shutdown
   *
   * @param e the exception that caused the shutdown
   */
  public void shutdown(Throwable e) throws ZernovaException {
    I18nMessage msg = fatalErrorWhileRunning();
    ZernovaException muleException = getRootMuleException(e);
    if (muleException != null) {
      logger.error(muleException.getDetailedMessage());
    } else {
      logger.error(msg.toString() + " " + e.getMessage(), e);
    }
    List<String> msgs = new ArrayList<>();
    msgs.add(msg.getMessage());
    Throwable root = getRootException(e);
    msgs.add(root.getMessage() + " (" + root.getClass().getName() + ")");
    msgs.add(" ");
    msgs.add(fatalErrorInShutdown().getMessage());
    String shutdownMessage = getBoilerPlate(msgs, '*', 80);
    logger.error(shutdownMessage);

    try {
      doShutdown();
    } catch (ZernovaException ex) {
      ex.addSuppressed(e);
      throw ex;
    }
  }

  /**
   * shutdown the server. This just displays the time the server shut down
   */
  @Override
  public void shutdown() throws ZernovaException {
    logger.info("ZERNOVA Runtime shutting down due to normal shutdown request");

    doShutdown();
  }

  protected void doShutdown() throws ZernovaException {
    unregisterShutdownHook();
    stop();
  }

  public void stop() throws ZernovaException {
    if (deploymentService != null) {
      deploymentService.stop();
    }

    if (muleLockFactory != null) {
      muleLockFactory.dispose();
    }

    if (extensionModelLoaderRepository != null) {
      stopIfNeeded(extensionModelLoaderRepository);
    }

    coreExtensionManager.stop();
    coreExtensionManager.dispose();

    if (serviceManager != null) {
      serviceManager.stop();
    }

    stopIfNeeded(containerServices);

    LoggerContextFactory defaultLogManagerFactory = getFactory();
    if (defaultLogManagerFactory instanceof ZernovaLog4jContextFactory) {
      ((ZernovaLog4jContextFactory) defaultLogManagerFactory).dispose();
    }

    if (log4jContextFactory != null && log4jContextFactory != defaultLogManagerFactory) {
      log4jContextFactory.dispose();
    }

    if (artifactResourcesRegistry.getDescriptorLoaderRepository() != null) {
      disposeIfNeeded(artifactResourcesRegistry.getDescriptorLoaderRepository(), logger);
    }

    if (repositoryService != null) {
      disposeIfNeeded(repositoryService, logger);
    }

    disposeIfNeeded(artifactResourcesRegistry, logger);
  }

  public Logger getLogger() {
    return logger;
  }

  public void registerShutdownHook() {
    if (muleShutdownHook == null) {
      muleShutdownHook = new ZernovaShutdownHook();
    } else {
      getRuntime().removeShutdownHook(muleShutdownHook);
    }
    getRuntime().addShutdownHook(muleShutdownHook);
  }

  public void unregisterShutdownHook() {
    if (muleShutdownHook != null) {
      getRuntime().removeShutdownHook(muleShutdownHook);
    }
  }

  // /////////////////////////////////////////////////////////////////
  // Getters and setters
  // /////////////////////////////////////////////////////////////////

  /**
   * This class is installed only for DefaultZernovaContainer running as commandline app. A clean Mule shutdown can be achieved by
   * disposing the {@link DefaultZernovaContext}.
   */
  private class ZernovaShutdownHook extends Thread {

    public ZernovaShutdownHook() {
      super("Mule.shutdown.hook");
    }

    @Override
    public void run() {
      try {
        DefaultZernovaContainer.this.stop();
      } catch (ZernovaException e) {
        logger.warn("Error stopping mule container", e);
      }
    }
  }

  /**
   * @return {@link DeploymentService} of the runtime.
   */
  public DeploymentService getDeploymentService() {
    return deploymentService;
  }

  /**
   * @return {@link ArtifactClassLoader} of the runtime.
   */
  public ArtifactClassLoader getContainerClassLoader() {
    return artifactResourcesRegistry.getContainerClassLoader();
  }

  public <S> void setService(Class<S> serviceInterface, S serviceInstance) {
    containerServices.put(serviceInterface, serviceInstance);
  }
}

