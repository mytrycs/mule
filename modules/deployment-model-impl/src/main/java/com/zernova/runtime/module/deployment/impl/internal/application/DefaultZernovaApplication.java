/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application;

import static com.zernova.runtime.api.connectivity.ConnectivityTestingService.CONNECTIVITY_TESTING_SERVICE_KEY;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.metadata.MetadataService.METADATA_SERVICE_KEY;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.api.value.ValueProviderService.VALUE_PROVIDER_SERVICE_KEY;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.context.notification.ZernovaContextNotification.CONTEXT_DISPOSED;
import static com.zernova.runtime.core.api.context.notification.ZernovaContextNotification.CONTEXT_INITIALISED;
import static com.zernova.runtime.core.api.context.notification.ZernovaContextNotification.CONTEXT_STARTED;
import static com.zernova.runtime.core.api.context.notification.ZernovaContextNotification.CONTEXT_STOPPED;
import static com.zernova.runtime.core.api.data.sample.SampleDataService.SAMPLE_DATA_SERVICE_KEY;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.core.internal.util.splash.SplashScreen.miniSplash;
import static com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor.DEFAULT_DOMAIN_NAME;
import static com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactContextBuilder.newBuilder;
import static com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainManager.isCompatibleBundle;
import static com.zernova.runtime.module.deployment.impl.internal.util.DeploymentPropertiesUtils.resolveDeploymentProperties;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.connectivity.ConnectivityTestingService;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.api.metadata.MetadataService;
import com.zernova.runtime.api.notification.IntegerAction;
import com.zernova.runtime.api.notification.Notification.Action;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.api.value.ValueProviderService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotification;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;
import com.zernova.runtime.core.api.data.sample.SampleDataService;
import com.zernova.runtime.core.internal.lifecycle.phases.NotInLifecyclePhase;
import com.zernova.runtime.deployment.model.api.DeploymentInitException;
import com.zernova.runtime.deployment.model.api.DeploymentStartException;
import com.zernova.runtime.deployment.model.api.InstallException;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.application.ApplicationStatus;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.module.deployment.impl.internal.artifact.AbstractDeployableArtifact;
import com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactContextBuilder;
import com.zernova.runtime.module.deployment.impl.internal.domain.AmbiguousDomainReferenceException;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainNotFoundException;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainRepository;
import com.zernova.runtime.module.deployment.impl.internal.domain.IncompatibleDomainException;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultZernovaApplication extends AbstractDeployableArtifact<ApplicationDescriptor> implements Application {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultZernovaApplication.class);
  private static final Logger SPLASH_LOGGER = getLogger("com.zernova.runtime.core.internal.logging");

  protected final ApplicationDescriptor descriptor;
  private final DomainRepository domainRepository;
  private final List<ArtifactPlugin> artifactPlugins;
  private final ServiceRepository serviceRepository;
  private final ExtensionModelLoaderRepository extensionModelLoaderRepository;
  private final ClassLoaderRepository classLoaderRepository;
  private final File location;
  private final MemoryManagementService memoryManagementService;
  private final ExpressionLanguageMetadataService expressionLanguageMetadataService;
  private final ArtifactConfigurationProcessor artifactConfigurationProcessor;
  private final Consumer<ClassLoader> actionOnMuleArtifactDeployment;
  private ApplicationStatus status;

  protected ZernovaContextListener muleContextListener;
  private NotificationListener<ZernovaContextNotification> statusListener;
  private final ApplicationPolicyProvider policyManager;

  private NotificationListenerRegistry notificationRegistrer;

  public DefaultZernovaApplication(ApplicationDescriptor descriptor,
                                ZernovaDeployableArtifactClassLoader deploymentClassLoader,
                                List<ArtifactPlugin> artifactPlugins, DomainRepository domainRepository,
                                ServiceRepository serviceRepository,
                                ExtensionModelLoaderRepository extensionModelLoaderRepository, File location,
                                ClassLoaderRepository classLoaderRepository,
                                ApplicationPolicyProvider applicationPolicyProvider,
                                MemoryManagementService memoryManagementService,
                                ArtifactConfigurationProcessor artifactConfigurationProcessor) {
    this(descriptor, deploymentClassLoader, artifactPlugins, domainRepository, serviceRepository, extensionModelLoaderRepository,
         location, classLoaderRepository, applicationPolicyProvider, memoryManagementService,
         artifactConfigurationProcessor, cl -> {
         });

  }

  public DefaultZernovaApplication(ApplicationDescriptor descriptor,
                                ZernovaDeployableArtifactClassLoader deploymentClassLoader,
                                List<ArtifactPlugin> artifactPlugins, DomainRepository domainRepository,
                                ServiceRepository serviceRepository,
                                ExtensionModelLoaderRepository extensionModelLoaderRepository, File location,
                                ClassLoaderRepository classLoaderRepository,
                                ApplicationPolicyProvider applicationPolicyProvider,
                                MemoryManagementService memoryManagementService,
                                ArtifactConfigurationProcessor artifactConfigurationProcessor,
                                Consumer<ClassLoader> actionOnMuleArtifactDeployment) {
    super("app", "application", deploymentClassLoader);
    this.descriptor = descriptor;
    this.domainRepository = domainRepository;
    this.serviceRepository = serviceRepository;
    this.extensionModelLoaderRepository = extensionModelLoaderRepository;
    this.classLoaderRepository = classLoaderRepository;
    this.artifactPlugins = artifactPlugins;
    this.location = location;
    this.policyManager = applicationPolicyProvider;
    this.memoryManagementService = memoryManagementService;
    this.expressionLanguageMetadataService = getExpressionLanguageMetadataService(serviceRepository);
    this.artifactConfigurationProcessor = artifactConfigurationProcessor;
    this.actionOnMuleArtifactDeployment = actionOnMuleArtifactDeployment;
    updateStatusFor(NotInLifecyclePhase.PHASE_NAME);
    if (this.deploymentClassLoader == null) {
      throw new IllegalArgumentException("Classloader cannot be null");
    }
  }

  private static ExpressionLanguageMetadataService getExpressionLanguageMetadataService(ServiceRepository serviceRepository) {
    if (serviceRepository == null) {
      return null;
    }
    return serviceRepository.getServices().stream()
        .filter(ExpressionLanguageMetadataService.class::isInstance)
        .findFirst()
        .map(ExpressionLanguageMetadataService.class::cast)
        .orElse(null);
  }

  @Override
  public void setMuleContextListener(ZernovaContextListener muleContextListener) {
    checkArgument(muleContextListener != null, "setMuleContextListener cannot be null");

    this.muleContextListener = muleContextListener;
  }

  @Override
  public void install() {
    withContextClassLoader(null, () -> {
      SPLASH_LOGGER.info(miniSplash(format("New %s '%s'", shortArtifactType, descriptor.getName())));
    });
    // set even though it might be redundant, just in case the app is been redeployed
    updateStatusFor(NotInLifecyclePhase.PHASE_NAME);

    try {
      for (String configFile : this.descriptor.getConfigResources()) {
        URL configFileUrl = getArtifactClassLoader().getClassLoader().getResource(configFile);
        if (configFileUrl == null) {
          String message = format("Config for %s '%s' not found: %s", shortArtifactType, getArtifactName(), configFile);
          throw new InstallException(createStaticMessage(message));
        }
      }
    } catch (Exception e) {
      setStatusToFailed();
      throw e;
    }
  }

  @Override
  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public Domain getDomain() {
    try {
      return getApplicationDomain(domainRepository, descriptor);
    } catch (DomainNotFoundException | IncompatibleDomainException | AmbiguousDomainReferenceException e) {
      return null;
    }
  }

  @Override
  public void start() {
    withContextClassLoader(null, () -> {
      SPLASH_LOGGER.info(miniSplash(format("Starting %s '%s'", shortArtifactType, descriptor.getName())));
    });
    try {
      this.artifactContext.getMuleContext().start();
      persistArtifactState(START);

      // null CCL ensures we log at 'system' level
      // TODO getDomainClassLoader a more usable wrapper for any logger to be logged at sys level
      withContextClassLoader(null, () -> {
        ApplicationStartedSplashScreen splashScreen = new ApplicationStartedSplashScreen();
        splashScreen.createMessage(descriptor);
        SPLASH_LOGGER.info(splashScreen.toString());
      });
    } catch (Exception e) {
      setStatusToFailed();

      // log it here so it ends up in app log, sys log will only log a message without stacktrace
      if (e instanceof ZernovaException) {
        LOGGER.error(((ZernovaException) e).getDetailedMessage());
      } else {
        LOGGER.error(null, getRootCause(e));
      }

      throw new DeploymentStartException(createStaticMessage(format("Error starting %s '%s'", artifactType,
                                                                    descriptor.getName())),
                                         e);
    }
  }

  @Override
  public void init() {
    doInit(false, false, false);
  }

  @Override
  public void initTooling() {
    doInit(false, false, true);
  }

  private void doInit(boolean lazy, boolean disableXmlValidations, boolean addToolingObjectsToRegistry) {
    withContextClassLoader(null, () -> {
      SPLASH_LOGGER.info(miniSplash(format("Initializing %s '%s'", shortArtifactType, descriptor.getName())));
    });
    try {
      ArtifactContextBuilder artifactBuilder =
          newBuilder()
              .setArtifactProperties(merge(descriptor.getAppProperties(), getProperties()))
              .setArtifactType(APP)
              .setArtifactConfigurationProcessor(null)
              .setDataFolderName(descriptor.getDataFolderName())
              .setArtifactName(descriptor.getName())
              .setArtifactInstallationDirectory(descriptor.getArtifactLocation())
              .setArtifactConfigurationProcessor(artifactConfigurationProcessor)
              .setConfigurationFiles(descriptor.getConfigResources().toArray(new String[descriptor.getConfigResources().size()]))
              .setDefaultEncoding(descriptor.getEncoding())
              .setArtifactPlugins(artifactPlugins)
              .setExecutionClassloader(deploymentClassLoader.getClassLoader())
              .setActionOnMuleArtifactDeployment(actionOnMuleArtifactDeployment)
              .setEnableLazyInit(lazy)
              .setDisableXmlValidations(disableXmlValidations)
              .setAddToolingObjectsToRegistry(addToolingObjectsToRegistry)
              .setServiceRepository(serviceRepository)
              .setExtensionModelLoaderRepository(extensionModelLoaderRepository)
              .setClassLoaderRepository(classLoaderRepository)
              .setArtifactDeclaration(descriptor.getArtifactDeclaration())
              .setProperties(ofNullable(resolveDeploymentProperties(descriptor.getDataFolderName(),
                                                                    descriptor.getDeploymentProperties())))
              .setPolicyProvider(policyManager)
              .setMemoryManagementService(memoryManagementService)
              .setExpressionLanguageMetadataService(expressionLanguageMetadataService)
              .setArtifactCoordinates(descriptor.getBundleDescriptor());

      Domain domain = getApplicationDomain(domainRepository, descriptor);
      if (domain.getArtifactContext() != null) {
        artifactBuilder.setParentArtifact(domain);
      }
      if (muleContextListener != null) {
        artifactBuilder.setMuleContextListener(muleContextListener);
      }
      artifactContext = artifactBuilder.build();
      setMuleContext(artifactContext.getMuleContext(), artifactContext.getRegistry());
      LOGGER.debug(artifactContext.getMuleContext().getStatistics().getFlowSummaryStatistics().toString());
    } catch (Exception e) {
      setStatusToFailed();

      // log it here so it ends up in app log, sys log will only log a message without stacktrace
      LOGGER.error(e.getMessage(), getRootCause(e));
      throw new DeploymentInitException(createStaticMessage(getRootCauseMessage(e)), e);
    }
  }

  private Properties getProperties() {
    if (!descriptor.getDeploymentProperties().isPresent()) {
      return new Properties();
    }

    return descriptor.getDeploymentProperties().get();
  }

  private Map<String, String> merge(Map<String, String> properties, Properties deploymentProperties) {
    if (deploymentProperties == null) {
      return properties;
    }

    Map<String, String> mergedProperties = new HashMap<>();
    mergedProperties.putAll(properties);
    for (Map.Entry<Object, Object> entry : deploymentProperties.entrySet()) {
      mergedProperties.put(entry.getKey().toString(), entry.getValue().toString());
    }

    return mergedProperties;
  }

  @Override
  public void lazyInit() {
    doInit(true, true, false);
  }

  @Override
  public void lazyInit(boolean disableXmlValidations) {
    doInit(true, disableXmlValidations, false);
  }

  @Override
  public void lazyInitTooling(boolean disableXmlValidations) {
    doInit(true, disableXmlValidations, true);
  }

  protected void setArtifactContext(final ArtifactContext artifactContext) {
    this.artifactContext = artifactContext;
    setMuleContext(artifactContext.getMuleContext(), artifactContext.getRegistry());
  }

  private void setMuleContext(final ZernovaContext muleContext, Registry registry) {
    statusListener = new ZernovaContextNotificationListener<ZernovaContextNotification>() {

      @Override
      public boolean isBlocking() {
        return false;
      }

      @Override
      public void onNotification(ZernovaContextNotification notification) {
        Action action = notification.getAction();
        if (new IntegerAction(CONTEXT_INITIALISED).equals(action) || new IntegerAction(CONTEXT_STARTED).equals(action)
            || new IntegerAction(CONTEXT_STOPPED).equals(action) || new IntegerAction(CONTEXT_DISPOSED).equals(action)) {
          updateStatusFor(muleContext.getLifecycleManager().getCurrentPhase());
        }
      }
    };

    notificationRegistrer = registry.lookupByType(NotificationListenerRegistry.class).get();
    notificationRegistrer.registerListener(statusListener);
  }

  private void updateStatusFor(String phase) {
    status = ApplicationStatusMapper.getApplicationStatus(phase);
  }

  private void setStatusToFailed() {
    if (artifactContext != null) {
      notificationRegistrer.unregisterListener(statusListener);
    }

    status = ApplicationStatus.DEPLOYMENT_FAILED;
  }

  @Override
  public Registry getRegistry() {
    return artifactContext != null ? artifactContext.getRegistry() : null;
  }

  @Override
  public File getLocation() {
    return location;
  }

  @Override
  public ConnectivityTestingService getConnectivityTestingService() {
    return (ConnectivityTestingService) artifactContext.getRegistry().lookupByName(CONNECTIVITY_TESTING_SERVICE_KEY).get();
  }

  @Override
  public MetadataService getMetadataService() {
    return (MetadataService) artifactContext.getRegistry().lookupByName(METADATA_SERVICE_KEY).get();
  }

  @Override
  public ValueProviderService getValueProviderService() {
    return (ValueProviderService) artifactContext.getRegistry().lookupByName(VALUE_PROVIDER_SERVICE_KEY).get();
  }

  @Override
  public SampleDataService getSampleDataService() {
    return (SampleDataService) artifactContext.getRegistry().lookupByName(SAMPLE_DATA_SERVICE_KEY).get();
  }

  @Override
  public String getArtifactName() {
    return descriptor.getName();
  }

  @Override
  public String getArtifactId() {
    return deploymentClassLoader.getArtifactId();
  }

  @Override
  public File[] getResourceFiles() {
    return descriptor.getConfigResources().stream()
        .map(configFile -> new File(getLocation(), configFile))
        .collect(Collectors.toList())
        .toArray(new File[descriptor.getConfigResources().size()]);
  }

  @Override
  public ArtifactClassLoader getArtifactClassLoader() {
    return deploymentClassLoader;
  }

  @Override
  public ApplicationStatus getStatus() {
    return status;
  }

  @Override
  public RegionClassLoader getRegionClassLoader() {
    ClassLoader parentClassLoader = deploymentClassLoader.getClassLoader().getParent();

    if (parentClassLoader instanceof RegionClassLoader) {
      return (RegionClassLoader) parentClassLoader;
    } else {
      throw new IllegalStateException("Application is not a region owner");
    }
  }

  @Override
  public ApplicationPolicyProvider getPolicyManager() {
    return policyManager;
  }

  @Override
  public List<ArtifactPlugin> getArtifactPlugins() {
    return artifactPlugins;
  }

  protected ArtifactClassLoader getDeploymentClassLoader() {
    return deploymentClassLoader;
  }

  @Override
  public String toString() {
    return format("%s[%s]@%s", getClass().getName(), descriptor.getName(), Integer.toHexString(System.identityHashCode(this)));
  }

  static Domain getApplicationDomain(DomainRepository domainRepository, ApplicationDescriptor descriptor)
      throws DomainNotFoundException, IncompatibleDomainException, AmbiguousDomainReferenceException {
    Domain resolvedDomain = resolveApplicationDomain(domainRepository, descriptor);
    descriptor.setDomainName(resolvedDomain.getDescriptor().getName());
    return resolvedDomain;
  }

  private static Domain resolveApplicationDomain(DomainRepository domainRepository, ApplicationDescriptor descriptor)
      throws DomainNotFoundException, IncompatibleDomainException, AmbiguousDomainReferenceException {
    String configuredDomainName = descriptor.getDomainName();
    Optional<BundleDescriptor> domainBundleDescriptor = descriptor.getDomainDescriptor();

    boolean shouldUseDefaultDomain = (configuredDomainName == null) || DEFAULT_DOMAIN_NAME.equals(configuredDomainName);
    if (!shouldUseDefaultDomain && !domainBundleDescriptor.isPresent()) {
      throw new IllegalStateException(format("Dependency for domain '%s' was not declared", configuredDomainName));
    }

    if (!domainBundleDescriptor.isPresent()) {
      return domainRepository.getDomain(DEFAULT_DOMAIN_NAME);
    }

    if (configuredDomainName != null) {
      Domain foundDomain = domainRepository.getDomain(configuredDomainName);
      if (isCompatibleBundle(foundDomain.getDescriptor().getBundleDescriptor(), domainBundleDescriptor.get())) {
        return foundDomain;
      } else {
        throw new IncompatibleDomainException(configuredDomainName, foundDomain);
      }
    }

    return domainRepository.getCompatibleDomain(domainBundleDescriptor.get());
  }

}
