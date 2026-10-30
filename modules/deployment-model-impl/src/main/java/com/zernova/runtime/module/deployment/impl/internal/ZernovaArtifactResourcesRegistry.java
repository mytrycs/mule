/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.container.api.ContainerClassLoaderProvider.createContainerClassLoader;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getAppDataFolder;
import static com.zernova.runtime.core.api.config.FeatureFlaggingRegistry.getInstance;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_CONTAINER_FEATURE_MANAGEMENT_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_MEMORY_MANAGEMENT_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.SERVER_NOTIFICATION_MANAGER;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.internal.profiling.AbstractProfilingService.configureEnableProfilingService;
import static com.zernova.runtime.deployment.model.api.artifact.ArtifactDescriptorFactoryProvider.artifactDescriptorFactoryProvider;
import static com.zernova.runtime.deployment.model.api.builder.DeployableArtifactClassLoaderFactoryProvider.domainClassLoaderFactory;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository.getExtensionModelLoaderManager;
import static com.zernova.runtime.module.artifact.activation.internal.classloader.ArtifactClassLoaderResolverConstants.CONTAINER_CLASS_LOADER;
import static com.zernova.runtime.module.artifact.activation.internal.classloader.ArtifactClassLoaderResolverConstants.MODULE_REPOSITORY;
import static com.zernova.runtime.module.license.api.LicenseValidatorProvider.discoverLicenseValidator;
import static com.zernova.runtime.module.service.api.artifact.ServiceClassLoaderFactoryProvider.serviceClassLoaderFactory;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.deployment.meta.ZernovaApplicationModel;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.container.internal.FilteringContainerClassLoader;
import com.zernova.runtime.core.api.config.FeatureContext;
import com.zernova.runtime.core.api.config.FeatureFlaggingRegistry;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.internal.config.FeatureFlaggingServiceBuilder;
import com.zernova.runtime.core.internal.lock.ServerLockFactory;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.registry.SimpleRegistry;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.DescriptorLoaderRepositoryFactory;
import com.zernova.runtime.deployment.model.api.builder.ApplicationClassLoaderBuilderFactory;
import com.zernova.runtime.deployment.model.api.builder.DomainClassLoaderBuilderFactory;
import com.zernova.runtime.deployment.model.api.builder.RegionPluginClassLoadersFactory;
import com.zernova.runtime.deployment.model.api.plugin.resolver.PluginDependenciesResolver;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.deployment.model.internal.DefaultRegionPluginClassLoadersFactory;
import com.zernova.runtime.deployment.model.internal.policy.PolicyTemplateClassLoaderFactory;
import com.zernova.runtime.internal.memory.management.ArtifactMemoryManagementService;
import com.zernova.runtime.internal.memory.management.DefaultMemoryManagementService;
import com.zernova.runtime.internal.memory.management.ProfiledMemoryManagementService;
import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.activation.internal.classloader.DefaultArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.DefaultNativeLibraryFinderFactory;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.TrackingArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.TrackingDeployableArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.runtime.module.artifact.internal.util.ServiceRegistryDescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.application.ApplicationDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import com.zernova.runtime.module.deployment.impl.internal.artifact.AbstractDeployableDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.artifact.DefaultArtifactDescriptorFactoryProvider;
import com.zernova.runtime.module.deployment.impl.internal.artifact.DefaultClassLoaderManager;
import com.zernova.runtime.module.deployment.impl.internal.classloader.TrackingArtifactClassLoaderResolverDecorator;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainManager;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainRepository;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import com.zernova.runtime.module.deployment.impl.internal.policy.ApplicationPolicyTemplateClassLoaderBuilderFactory;
import com.zernova.runtime.module.deployment.impl.internal.policy.PolicyTemplateClassLoaderBuilderFactory;
import com.zernova.runtime.module.license.api.LicenseValidator;
import com.zernova.runtime.module.service.api.artifact.ServiceDescriptor;
import com.zernova.runtime.module.service.api.manager.ServiceManager;
import com.zernova.runtime.module.service.internal.discoverer.DefaultServiceDiscoverer;
import com.zernova.runtime.module.service.internal.discoverer.FileSystemServiceProviderDiscoverer;
import com.zernova.runtime.module.service.internal.discoverer.ReflectionServiceResolver;
import com.zernova.runtime.module.service.internal.manager.DefaultServiceRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Registry of mule artifact resources required to construct new artifacts.
 *
 * @since 4.0
 */
public class ZernovaArtifactResourcesRegistry extends SimpleRegistry {

  private static final String CONTAINER_FEATURE_CONTEXT_NAME = "Container";
  private final ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader;
  private final DefaultDomainManager domainManager;
  private final DefaultDomainFactory domainFactory;
  private final DefaultApplicationFactory applicationFactory;
  private final DeployableArtifactClassLoaderFactory<DomainDescriptor> domainClassLoaderFactory;
  private final ArtifactClassLoader containerClassLoader;
  private final ServiceManager serviceManager;
  private final ExtensionModelLoaderRepository extensionModelLoaderRepository;
  private final DefaultClassLoaderManager artifactClassLoaderManager;
  private final ApplicationClassLoaderBuilderFactory applicationClassLoaderBuilderFactory;
  private final DomainClassLoaderBuilderFactory domainClassLoaderBuilderFactory;
  private final DomainDescriptorFactory domainDescriptorFactory;
  private final PluginDependenciesResolver pluginDependenciesResolver;
  private final ServiceRegistryDescriptorLoaderRepository descriptorLoaderRepository;
  private final RegionPluginClassLoadersFactory pluginClassLoadersFactory;
  private final ArtifactConfigurationProcessor artifactConfigurationProcessor;
  private final AbstractDeployableDescriptorFactory<ZernovaApplicationModel, ApplicationDescriptor> toolingApplicationDescriptorFactory;
  private ProfiledMemoryManagementService memoryManagementService = DefaultMemoryManagementService.getInstance();
  private final ProfilingService containerProfilingService;
  private final ServerNotificationManager serverNotificationManager;
  private final LicenseValidator licenseValidator;


  /**
   * Builds a {@link ZernovaArtifactResourcesRegistry} instance
   */
  public static class Builder {

    private ModuleRepository moduleRepository;
    private ArtifactConfigurationProcessor artifactConfigurationProcessor;
    private ProfiledMemoryManagementService memoryManagementService;
    private final Set<String> bootPackages = new HashSet<>();
    private final Set<String> additionalResourceDirectories = new HashSet<>();
    private Consumer<ClassLoader> actionOnMuleArtifactDeployment = cl -> {
    };

    /**
     * Configures the {@link ModuleRepository} to use
     *
     * @param moduleRepository provides access to the modules available on the container. Non null.
     * @return same builder instance
     */
    public Builder moduleRepository(ModuleRepository moduleRepository) {
      checkArgument(moduleRepository != null, "moduleRepository cannot be null");

      this.moduleRepository = moduleRepository;
      return this;
    }

    /**
     * Configures the {@link ArtifactConfigurationProcessor} to use.
     *
     * @param artifactConfigurationProcessor the processor to use for building the application model.
     * @return the same builder instance
     *
     * @since 4.5
     */
    public Builder artifactConfigurationProcessor(ArtifactConfigurationProcessor artifactConfigurationProcessor) {
      checkArgument(artifactConfigurationProcessor != null, "artifactConfigurationProcessor cannot be null");

      this.artifactConfigurationProcessor = artifactConfigurationProcessor;
      return this;
    }

    /**
     * Configures the container {@link MemoryManagementService} to use.
     *
     * @param memoryManagementService the memory management service at container level.
     * @return the same builder instance
     *
     * @since 4.5
     */
    public Builder withMemoryManagementService(ProfiledMemoryManagementService memoryManagementService) {
      this.memoryManagementService = memoryManagementService;
      return this;
    }

    public Builder withBootPackage(String pkg) {
      this.bootPackages.add(pkg);
      return this;
    }

    public Builder withAdditionalResourceDirectory(String resourceDirectory) {
      this.additionalResourceDirectories.add(resourceDirectory);
      return this;
    }

    /**
     * An action to perform on the classloader when the artifact is deployed.
     *
     * @since 4.7.0
     *
     * @param actionOnMuleArtifactDeployment the action to be performed.
     * @return the current builder.
     * @throws NullPointerException if {@param actionOnMuleArtifactDeployment} is null.
     */
    public Builder withActionOnMuleArtifactDeployment(Consumer<ClassLoader> actionOnMuleArtifactDeployment) {
      requireNonNull(actionOnMuleArtifactDeployment);
      this.actionOnMuleArtifactDeployment = actionOnMuleArtifactDeployment;
      return this;
    }

    /**
     * Builds the desired instance
     *
     * @return a new {@link ZernovaArtifactResourcesRegistry} with the provided configuration.
     */
    public ZernovaArtifactResourcesRegistry build() {
      ArtifactClassLoader containerClassLoader;
      if (moduleRepository == null) {
        moduleRepository = MODULE_REPOSITORY;
      }
      if (bootPackages.isEmpty() && additionalResourceDirectories.isEmpty()) {
        containerClassLoader = createContainerClassLoader(moduleRepository);
      } else {
        containerClassLoader = createContainerClassLoader(moduleRepository, bootPackages, additionalResourceDirectories);
      }

      try {
        return new ZernovaArtifactResourcesRegistry(containerClassLoader, moduleRepository, artifactConfigurationProcessor,
                                                 memoryManagementService, actionOnMuleArtifactDeployment);
      } catch (RegistrationException e) {
        throw new ZernovaRuntimeException(e);
      }
    }
  }

  @Override
  protected void doDispose() {
    disposeIfNeeded(licenseValidator, logger);
    super.doDispose();
  }

  /**
   * Creates a repository for resources required for mule artifacts.
   */
  private ZernovaArtifactResourcesRegistry(ArtifactClassLoader containerClassLoader,
                                        ModuleRepository moduleRepository,
                                        ArtifactConfigurationProcessor artifactConfigurationProcessor,
                                        ProfiledMemoryManagementService memoryManagementService,
                                        Consumer<ClassLoader> actionOnMuleArtifactDeployment)
      throws RegistrationException {
    // Creates a registry to be used as an injector.
    super(null);

    // A "container level" notification server is created and registered in order to be injectable
    serverNotificationManager = ServerNotificationManager
        .createDefaultNotificationManager(new LazyValue<>(this::getSchedulerService),
                                          new LazyValue<>(() -> "containerServerNotificationManager"));
    registerObject(SERVER_NOTIFICATION_MANAGER, serverNotificationManager);

    // TODO: W-11631353: Refactor creation of memory management at container and artifact level
    if (memoryManagementService != null) {
      this.memoryManagementService = memoryManagementService;
    }

    this.containerProfilingService = new DefaultProfilingService();

    this.memoryManagementService.setProfilingService(containerProfilingService);

    MemoryManagementService artifactMemoryManagementService = new ArtifactMemoryManagementService(this.memoryManagementService);

    // Registers the memory management so that this can be injected.
    registerObject(MULE_MEMORY_MANAGEMENT_SERVICE, artifactMemoryManagementService);
    registerObject(MULE_CONTAINER_FEATURE_MANAGEMENT_SERVICE, getContainerFeatureFlaggingService());

    this.containerClassLoader = containerClassLoader;
    artifactClassLoaderManager = new DefaultClassLoaderManager();
    licenseValidator = discoverLicenseValidator(currentThread().getContextClassLoader());

    domainManager = new DefaultDomainManager();
    this.domainClassLoaderFactory =
        trackDeployableArtifactClassLoaderFactory(domainClassLoaderFactory(name -> getAppDataFolder(name)));

    final AbstractArtifactDescriptorFactory<ZernovaPluginModel, ArtifactPluginDescriptor> artifactPluginDescriptorFactory =
        artifactDescriptorFactoryProvider()
            .createArtifactPluginDescriptorFactory(new DescriptorLoaderRepositoryFactory().createDescriptorLoaderRepository(),
                                                   ArtifactDescriptorValidatorBuilder.builder());
    artifactPluginDescriptorLoader = new ArtifactPluginDescriptorLoader(artifactPluginDescriptorFactory);
    descriptorLoaderRepository = new ServiceRegistryDescriptorLoaderRepository();

    ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder = ArtifactDescriptorValidatorBuilder.builder();

    domainDescriptorFactory = new DomainDescriptorFactory(artifactPluginDescriptorLoader, descriptorLoaderRepository,
                                                          artifactDescriptorValidatorBuilder);
    DeployableArtifactDescriptorFactory deployableArtifactDescriptorFactory =
        DeployableArtifactDescriptorFactory.defaultArtifactDescriptorFactory();

    ArtifactClassLoaderResolver artifactClassLoaderResolver =
        new TrackingArtifactClassLoaderResolverDecorator(artifactClassLoaderManager,
                                                         new DefaultArtifactClassLoaderResolver(containerClassLoader,
                                                                                                moduleRepository,
                                                                                                new DefaultNativeLibraryFinderFactory(name -> getAppDataFolder(name))));

    pluginClassLoadersFactory = new DefaultRegionPluginClassLoadersFactory(artifactClassLoaderResolver);
    applicationClassLoaderBuilderFactory = new ApplicationClassLoaderBuilderFactory(artifactClassLoaderResolver);
    domainClassLoaderBuilderFactory = new DomainClassLoaderBuilderFactory(artifactClassLoaderResolver);

    this.artifactConfigurationProcessor = artifactConfigurationProcessor;

    ArtifactClassLoaderFactory<ServiceDescriptor> serviceClassLoaderFactory = serviceClassLoaderFactory();
    serviceManager =
        ServiceManager.create(new DefaultServiceDiscoverer(new FileSystemServiceProviderDiscoverer(containerClassLoader,
                                                                                                   trackArtifactClassLoaderFactory(serviceClassLoaderFactory),
                                                                                                   descriptorLoaderRepository,
                                                                                                   artifactDescriptorValidatorBuilder),
                                                           new ReflectionServiceResolver(new DefaultServiceRegistry(),
                                                                                         this,
                                                                                         (service, assembly) -> service)));

    extensionModelLoaderRepository = getExtensionModelLoaderManager();

    pluginDependenciesResolver =
        new DefaultArtifactDescriptorFactoryProvider().createBundlePluginDependenciesResolver(artifactPluginDescriptorFactory);
    domainFactory = new DefaultDomainFactory(domainDescriptorFactory, deployableArtifactDescriptorFactory, domainManager,
                                             artifactClassLoaderManager, serviceManager,
                                             domainClassLoaderBuilderFactory,
                                             extensionModelLoaderRepository, licenseValidator,
                                             this.memoryManagementService,
                                             artifactConfigurationProcessor);

    DeployableArtifactClassLoaderFactory<PolicyTemplateDescriptor> policyClassLoaderFactory =
        trackDeployableArtifactClassLoaderFactory(new PolicyTemplateClassLoaderFactory());
    PolicyTemplateClassLoaderBuilderFactory policyTemplateClassLoaderBuilderFactory =
        new ApplicationPolicyTemplateClassLoaderBuilderFactory(policyClassLoaderFactory, pluginClassLoadersFactory,
                                                               (FilteringContainerClassLoader) containerClassLoader);

    applicationFactory = new DefaultApplicationFactory(applicationClassLoaderBuilderFactory,
                                                       deployableArtifactDescriptorFactory,
                                                       domainManager, serviceManager,
                                                       extensionModelLoaderRepository,
                                                       artifactClassLoaderManager, policyTemplateClassLoaderBuilderFactory,
                                                       pluginDependenciesResolver,
                                                       licenseValidator,
                                                       this.memoryManagementService,
                                                       artifactConfigurationProcessor,
                                                       actionOnMuleArtifactDeployment);
    toolingApplicationDescriptorFactory =
        new ApplicationDescriptorFactory(artifactPluginDescriptorLoader, descriptorLoaderRepository,
                                         artifactDescriptorValidatorBuilder);
  }

  private FeatureFlaggingService getContainerFeatureFlaggingService() {
    FeatureFlaggingRegistry ffRegistry = getInstance();

    configureEnableProfilingService();

    return new FeatureFlaggingServiceBuilder()
        .withContext(new FeatureContext(new ZernovaVersion(getMuleManifest().getProductVersion()), CONTAINER_FEATURE_CONTEXT_NAME))
        .withMuleContextFlags(ffRegistry.getFeatureConfigurations())
        .withFeatureContextFlags(ffRegistry.getFeatureFlagConfigurations())
        .build();
  }

  private <T extends ArtifactDescriptor> ArtifactClassLoaderFactory<T> trackArtifactClassLoaderFactory(ArtifactClassLoaderFactory<T> artifactClassLoaderFactory) {
    return new TrackingArtifactClassLoaderFactory<>(artifactClassLoaderManager, artifactClassLoaderFactory);
  }

  private <T extends ArtifactDescriptor> DeployableArtifactClassLoaderFactory<T> trackDeployableArtifactClassLoaderFactory(DeployableArtifactClassLoaderFactory<T> deployableArtifactClassLoaderFactory) {
    return new TrackingDeployableArtifactClassLoaderFactory<>(artifactClassLoaderManager,
                                                              deployableArtifactClassLoaderFactory);
  }

  /**
   * @return a loader for creating the {@link ArtifactPluginDescriptor} from a zipped extension
   */
  public ArtifactPluginDescriptorLoader getArtifactPluginDescriptorLoader() {
    return artifactPluginDescriptorLoader;
  }

  /**
   * @return the domain factory used to create and register domains
   */
  public DefaultDomainFactory getDomainFactory() {
    return domainFactory;
  }

  /**
   * @return the domain repository where domains are registered once deployed.
   */
  public DomainRepository getDomainRepository() {
    return domainManager;
  }

  /**
   * @return the domain descriptor factory used to created domains.
   */
  public DomainDescriptorFactory getDomainDescriptorFactory() {
    return domainDescriptorFactory;
  }

  /**
   * @return factory for creating {@link Application} artifacts
   */
  public DefaultApplicationFactory getApplicationFactory() {
    return applicationFactory;
  }

  /**
   * @return factory for creating domain class loaders
   */
  public DeployableArtifactClassLoaderFactory<DomainDescriptor> getDomainClassLoaderFactory() {
    return domainClassLoaderFactory;
  }

  /**
   * @return the container class loader
   */
  public ArtifactClassLoader getContainerClassLoader() {
    return containerClassLoader;
  }

  /**
   * @return the manager of container services that must be included in each artifact
   */
  public ServiceManager getServiceManager() {
    return serviceManager;
  }

  /**
   * @return the manager of available extension loaders.
   */
  public ExtensionModelLoaderRepository getExtensionModelLoaderRepository() {
    return extensionModelLoaderRepository;
  }

  /**
   * @return the artifact classLoader manager
   */
  public DefaultClassLoaderManager getArtifactClassLoaderManager() {
    return artifactClassLoaderManager;
  }

  /**
   * @return the {@link AbstractDeployableDescriptorFactory}.
   */
  public AbstractDeployableDescriptorFactory<ZernovaApplicationModel, ApplicationDescriptor> getToolingApplicationDescriptorFactory() {
    return toolingApplicationDescriptorFactory;
  }

  /**
   * @return resolver for dependencies for plugins
   */
  public PluginDependenciesResolver getPluginDependenciesResolver() {
    return pluginDependenciesResolver;
  }

  /**
   * @return the processor to use for building the application model
   */
  public ArtifactConfigurationProcessor getArtifactConfigurationProcessor() {
    return artifactConfigurationProcessor;
  }

  /**
   * @return a {@link MemoryManagementService} that can be shared between deployable artifacts.
   */
  public MemoryManagementService getMemoryManagementService() {
    return memoryManagementService;
  }

  /**
   * @return a {@link ProfilingService} that can be shared between deployable artifacts.
   */
  public ProfilingService getContainerProfilingService() {
    return containerProfilingService;
  }

  /**
   * @return a {@link DescriptorLoaderRepository} that detects available implementations of
   *         {@link ClassLoaderConfigurationLoader}.
   */
  public DescriptorLoaderRepository getDescriptorLoaderRepository() {
    return descriptorLoaderRepository;
  }

  private SchedulerService getSchedulerService() {
    return (SchedulerService) serviceManager.getServices().stream().filter(s -> s instanceof SchedulerService).findFirst().get();
  }

}
