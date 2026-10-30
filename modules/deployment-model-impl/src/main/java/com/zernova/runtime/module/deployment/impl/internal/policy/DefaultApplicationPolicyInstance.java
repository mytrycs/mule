/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENABLE_POLICY_ISOLATION;
import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.SEPARATE_CLASSLOADER_FOR_POLICY_ISOLATION;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_IN_MEMORY_OBJECT_STORE_KEY;
import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_PERSISTENT_OBJECT_STORE_KEY;
import static com.zernova.runtime.api.util.collection.SmallMap.copy;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_LOCK_PROVIDER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TIME_SUPPLIER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.POLICY;
import static com.zernova.runtime.module.artifact.internal.util.FeatureFlaggingUtils.isFeatureEnabled;
import static com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactContextBuilder.newBuilder;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.config.ZernovaRuntimeFeature;
import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.notification.PolicyNotification;
import com.zernova.runtime.api.notification.PolicyNotificationListener;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.core.api.policy.PolicyInstance;
import com.zernova.runtime.core.api.policy.PolicyParametrization;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplate;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.internal.memory.management.ArtifactMemoryManagementService;
import com.zernova.runtime.internal.memory.management.DefaultMemoryManagementService;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactContextBuilder;
import com.zernova.runtime.module.deployment.impl.internal.policy.proxy.LifecycleFilterProxy;
import com.zernova.runtime.policy.api.PolicyPointcut;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link ApplicationPolicyInstance} that depends on a {@link PolicyTemplate} artifact.
 */
public class DefaultApplicationPolicyInstance implements ApplicationPolicyInstance {

  public static final String CLUSTER_MANAGER_ID = "_muleClusterManager";
  public static final String IS_SILENT_DEPLOY_PARAMETER_NAME = "isSilentDeploy";

  private final Application application;
  private final PolicyTemplate template;
  private final PolicyParametrization parametrization;
  private final ServiceRepository serviceRepository;
  private final ClassLoaderRepository classLoaderRepository;
  private final ExtensionModelLoaderRepository extensionModelLoaderRepository;
  private final ZernovaContextListener muleContextListener;
  private final ArtifactConfigurationProcessor artifactConfigurationProcessor;
  private ArtifactContext policyContext;
  private LazyValue<PolicyInstance> policyInstance;

  /**
   * Creates a new policy instance
   *
   * @param application                    application artifact owning the created policy. Non null
   * @param template                       policy template from which the instance will be created. Non null
   * @param parametrization                parameters used to configure the created instance. Non null
   * @param serviceRepository              repository of available services. Non null.
   * @param classLoaderRepository          contains the registered classloaders that can be used to load serialized classes. Non
   *                                       null.
   * @param extensionModelLoaderRepository {@link ExtensionModelLoaderRepository} with the available extension loaders. Non null.
   * @param muleContextListener            the listener to execute for specific events that occur on the {@link ZernovaContext} of
   *                                       the policy. May be {@code null}.
   * @param artifactConfigurationProcessor the processor to use for building the application model. Non null.
   */
  public DefaultApplicationPolicyInstance(Application application, PolicyTemplate template,
                                          PolicyParametrization parametrization,
                                          ServiceRepository serviceRepository,
                                          ClassLoaderRepository classLoaderRepository,
                                          ExtensionModelLoaderRepository extensionModelLoaderRepository,
                                          ZernovaContextListener muleContextListener,
                                          ArtifactConfigurationProcessor artifactConfigurationProcessor) {
    this.application = application;
    this.template = template;
    this.parametrization = parametrization;
    this.serviceRepository = serviceRepository;
    this.classLoaderRepository = classLoaderRepository;
    this.extensionModelLoaderRepository = extensionModelLoaderRepository;
    this.muleContextListener = muleContextListener;
    this.artifactConfigurationProcessor = artifactConfigurationProcessor;
  }

  private void initPolicyContext() throws InitialisationException {
    ArtifactContextBuilder artifactBuilder =
        newBuilder().setArtifactType(POLICY)
            .setArtifactProperties(copy(parametrization.getParameters()))
            .setArtifactName(parametrization.getId())
            .setArtifactConfigurationProcessor(artifactConfigurationProcessor)
            .setConfigurationFiles(parametrization.getConfig().getAbsolutePath())
            .setExecutionClassloader(template.getArtifactClassLoader().getClassLoader())
            .setServiceRepository(serviceRepository)
            .setClassLoaderRepository(classLoaderRepository)
            .setArtifactPlugins(getFeatureFlaggedArtifactPlugins(template.getDescriptor()))
            .setParentArtifact(application)
            .setExtensionManagerFactory(new PolicyExtensionManagerFactory(application, template, extensionModelLoaderRepository,
                                                                          isPolicyIsolationEnabled(template.getDescriptor())))
            .setMuleContextListener(muleContextListener)
            .setArtifactCoordinates(template.getDescriptor().getBundleDescriptor())
            .setMemoryManagementService(new ArtifactMemoryManagementService(DefaultMemoryManagementService.getInstance()));

    artifactBuilder.withServiceConfigurator(customizationService -> {
      Registry applicationRegistry = application.getArtifactContext().getRegistry();

      addPolicyCustomizationOverride(OBJECT_LOCK_PROVIDER, customizationService, applicationRegistry);
      addPolicyCustomizationOverride(BASE_PERSISTENT_OBJECT_STORE_KEY, customizationService, applicationRegistry);
      addPolicyCustomizationOverride(BASE_IN_MEMORY_OBJECT_STORE_KEY, customizationService, applicationRegistry);
      addPolicyCustomizationOverride(OBJECT_TIME_SUPPLIER, customizationService, applicationRegistry);
      addPolicyCustomizationOverride(CLUSTER_MANAGER_ID, customizationService, applicationRegistry);
    });
    try {
      policyContext = artifactBuilder.build();
      if (isSilentDeploy(this.parametrization) && policyContext.getMuleContext() instanceof DefaultZernovaContext) {
        ((DefaultZernovaContext) policyContext.getMuleContext()).setDebugSplashScreenLevel();
      }

      enableNotificationListeners(parametrization.getNotificationListeners());
      policyContext.getMuleContext().start();
    } catch (ZernovaException e) {
      throw new InitialisationException(createStaticMessage("Cannot create artifact context for the policy instance"), e, this);
    }
  }

  private boolean isPolicyIsolationEnabled(PolicyTemplateDescriptor descriptor) {
    return isFeatureEnabled(ENABLE_POLICY_ISOLATION, descriptor) &&
        isFeatureEnabled(SEPARATE_CLASSLOADER_FOR_POLICY_ISOLATION, descriptor);
  }

  /**
   * Applies the {@link ZernovaRuntimeFeature#ENABLE_POLICY_ISOLATION} feature to the policy artifact plugins list.
   *
   * @return The policy artifact plugins.
   */
  private List<ArtifactPlugin> getFeatureFlaggedArtifactPlugins(ArtifactDescriptor policyArtifactDescriptor) {
    if (isPolicyIsolationEnabled((PolicyTemplateDescriptor) policyArtifactDescriptor)) {
      // Returns all the artifact plugins that the policy depends on.
      return template.getOwnArtifactPlugins();
    } else {
      // Returns the artifact plugins that the policy depends and are not imported by the application.
      return template.getArtifactPlugins();
    }
  }

  private void addPolicyCustomizationOverride(String objectKey, CustomizationService customizationService,
                                              Registry applicationRegistry) {
    applicationRegistry
        .lookupByName(objectKey)
        .map(LifecycleFilterProxy::createLifecycleFilterProxy)
        .ifPresent(s -> customizationService.overrideDefaultServiceImpl(objectKey, s));
  }

  private void enableNotificationListeners(List<NotificationListener> notificationListeners) {
    NotificationListenerRegistry listenerRegistry =
        policyContext.getRegistry().lookupByType(NotificationListenerRegistry.class).get();

    policyContext.getMuleContext().getNotificationManager().addInterfaceToType(PolicyNotificationListener.class,
                                                                               PolicyNotification.class);
    notificationListeners.forEach(listenerRegistry::registerListener);
  }

  private PolicyInstance initPolicyInstance() {
    return policyContext.getRegistry().lookupByType(PolicyInstance.class).get();
  }

  @Override
  public PolicyPointcut getPointcut() {
    return parametrization.getPolicyPointcut();
  }

  @Override
  public int getOrder() {
    return parametrization.getOrder();
  }

  @Override
  public PolicyTemplate getPolicyTemplate() {
    return template;
  }

  @Override
  public void initialise() throws InitialisationException {
    if (policyInstance == null && policyContext == null) {
      initPolicyContext();
    }
    policyInstance = new LazyValue<>(this::initPolicyInstance);
  }

  @Override
  public void dispose() {
    if (policyContext != null) {
      policyContext.getMuleContext().dispose();
    }
  }

  @Override
  public Optional<Policy> getSourcePolicy() {
    return policyInstance.get().getSourcePolicyChain()
        .map(chain -> new Policy(chain, parametrization.getId()));
  }

  @Override
  public Optional<Policy> getOperationPolicy() {
    return policyInstance.get().getOperationPolicyChain()
        .map(chain -> new Policy(chain, parametrization.getId()));
  }


  /**
   * Check if the policy should avoid logging the splash screen.
   *
   * @param parametrization
   * @return true if the parametrization corresponds to a silent deploy. false otherwise.
   */
  private boolean isSilentDeploy(PolicyParametrization parametrization) {
    return parametrization.getParameters().getOrDefault(IS_SILENT_DEPLOY_PARAMETER_NAME, "false").equalsIgnoreCase("true");
  }
}
