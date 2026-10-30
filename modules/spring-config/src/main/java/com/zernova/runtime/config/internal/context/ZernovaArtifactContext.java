/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.DISABLE_ATTRIBUTE_PARAMETER_WHITESPACE_TRIMMING;
import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.DISABLE_POJO_TEXT_CDATA_WHITESPACE_TRIMMING;
import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.VALIDATE_APPLICATION_MODEL_WITH_REGION_CLASSLOADER;
import static com.zernova.runtime.ast.api.util.AstTraversalDirection.BOTTOM_UP;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.recursiveStreamWithHierarchy;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.validatorBuilder;
import static com.zernova.runtime.config.api.dsl.CoreDslConstants.NOTIFICATIONS_IDENTIFIER;
import static com.zernova.runtime.config.internal.context.AbstractSpringZernovaContextServiceConfigurator.getBeanDefinitionBuilder;
import static com.zernova.runtime.config.internal.dsl.spring.BeanDefinitionFactory.SPRING_SINGLETON_OBJECT;
import static com.zernova.runtime.config.internal.model.ApplicationModel.findComponentDefinitionModel;
import static com.zernova.runtime.config.internal.model.ApplicationModel.prepareAstForRuntime;
import static com.zernova.runtime.config.internal.model.ApplicationModelAstPostProcessor.AST_POST_PROCESSORS;
import static com.zernova.runtime.config.internal.model.properties.PropertiesHierarchyCreationUtils.createConfigurationAttributeResolver;
import static com.zernova.runtime.config.internal.parsers.generic.AutoIdUtils.uniqueValue;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONTEXT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_NOTIFICATION_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_REGISTRY;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.POLICY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.internal.el.function.ZernovaFunctionsBindingContextProvider.CORE_FUNCTIONS_PROVIDER_REGISTRY_KEY;
import static com.zernova.runtime.core.internal.exception.ErrorTypeLocatorFactory.createDefaultErrorTypeLocator;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.APP_CONFIG;
import static com.zernova.runtime.module.artifact.activation.api.ast.ArtifactAstUtils.parseArtifactExtensionModel;
import static com.zernova.runtime.module.artifact.activation.internal.ast.validation.AstValidationUtils.logWarningsAndThrowIfContainsErrors;
import static com.zernova.runtime.module.extension.internal.manager.ExtensionErrorsRegistrant.registerErrorMappings;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.beans.CachedIntrospectionResults.clearClassLoader;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.ioc.ConfigurableObjectProvider;
import com.zernova.runtime.api.ioc.ObjectProvider;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.stereotype.HasStereotypeModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.api.notification.ConnectionNotification;
import com.zernova.runtime.api.notification.ConnectionNotificationListener;
import com.zernova.runtime.api.notification.CustomNotification;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.ExtensionNotification;
import com.zernova.runtime.api.notification.ExtensionNotificationListener;
import com.zernova.runtime.api.notification.ManagementNotification;
import com.zernova.runtime.api.notification.ManagementNotificationListener;
import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.SecurityNotification;
import com.zernova.runtime.api.notification.SecurityNotificationListener;
import com.zernova.runtime.api.notification.TransactionNotification;
import com.zernova.runtime.api.notification.TransactionNotificationListener;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResult;
import com.zernova.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import com.zernova.runtime.config.internal.bean.NotificationConfig;
import com.zernova.runtime.config.internal.bean.NotificationConfig.EnabledNotificationConfig;
import com.zernova.runtime.config.internal.bean.ServerNotificationManagerConfigurator;
import com.zernova.runtime.config.internal.dsl.model.SpringComponentModel;
import com.zernova.runtime.config.internal.dsl.spring.BeanDefinitionFactory;
import com.zernova.runtime.config.internal.editors.ZernovaPropertyEditorRegistrar;
import com.zernova.runtime.config.internal.model.ApplicationModel;
import com.zernova.runtime.config.internal.model.ApplicationModelAstPostProcessor;
import com.zernova.runtime.config.internal.model.dsl.ClassLoaderResourceProvider;
import com.zernova.runtime.config.internal.model.dsl.config.PropertiesResolverConfigurationProperties;
import com.zernova.runtime.config.internal.processor.ComponentLocatorCreatePostProcessor;
import com.zernova.runtime.config.internal.processor.ZernovaInjectorProcessor;
import com.zernova.runtime.config.internal.validation.ast.ReusableArtifactAstDependencyGraphProvider;
import com.zernova.runtime.config.privileged.spring.ByteBuddySpringCachesManager;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotification;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.transformer.Converter;
import com.zernova.runtime.core.internal.config.DefaultResourceLocator;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.el.function.ZernovaFunctionsBindingContextProvider;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeRepository;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.internal.registry.TransformerResolver;
import com.zernova.runtime.core.internal.transaction.TransactionManagerFactory;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.runtime.module.artifact.internal.classloader.WithAttachedClassLoaders;
import com.zernova.runtime.module.extension.internal.component.AnnotatedObjectInvocationHandler;
import com.zernova.runtime.module.extension.internal.manager.CompositeArtifactExtensionManager;
import com.zernova.runtime.module.extension.internal.util.IntrospectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;

/**
 * <code>ZernovaArtifactContext</code> is a simple extension application context that allows resources to be loaded from the
 * Classpath of file system using the MuleBeanDefinitionReader.
 */
public class ZernovaArtifactContext extends AbstractRefreshableConfigApplicationContext {

  private static final Logger LOGGER = getLogger(ZernovaArtifactContext.class);

  public static final String INNER_BEAN_PREFIX = "(inner bean)";

  private final DefaultRegistry serviceDiscoverer;
  private final DefaultResourceLocator resourceLocator;
  private final PropertiesResolverConfigurationProperties configurationProperties;
  protected final MemoryManagementService memoryManagementService;
  private final ExpressionLanguageMetadataService expressionLanguageMetadataService;
  private ArtifactAst applicationModel;
  private final ZernovaContextWithRegistry muleContext;
  private final FeatureFlaggingService featureFlaggingService;
  private final ZernovaFunctionsBindingContextProvider coreFunctionsProvider;
  private final BeanDefinitionFactory beanDefinitionFactory;
  private final ArtifactType artifactType;
  private final BaseConfigurationComponentLocator baseComponentLocator;
  protected SpringConfigurationComponentLocator componentLocator;
  private final ContributedErrorTypeRepository errorTypeRepository;
  private final ContributedErrorTypeLocator errorTypeLocator;
  private final Map<String, String> artifactProperties;
  protected List<ConfigurableObjectProvider> objectProviders = new ArrayList<>();
  private final ExtensionManager extensionManager;
  // TODO W-10855416: remove this
  private final boolean validateAppModelWithRegionClassloader;

  /**
   * Parses configuration files creating a spring ApplicationContext which is used as a parent registry using the SpringRegistry
   * registry implementation to wraps the spring ApplicationContext
   *
   * @param muleContext                                the {@link ZernovaContext} that own this context
   * @param artifactAst                                the definition of the artifact to create a context for
   * @param parentConfigurationProperties              the resolver for properties from the parent artifact to be used as fallback
   *                                                   in this artifact.
   * @param baseConfigurationComponentLocator          indirection to the actual ConfigurationComponentLocator in the full
   *                                                   registry
   * @param errorTypeRepository                        repository where the errors of the artifact will be registered.
   * @param errorTypeLocator                           locator where the errors of the artifact will be registered.
   * @param artifactProperties                         map of properties that can be referenced from the
   *                                                   {@code artifactConfigResources} as external configuration values
   * @param artifactType                               the type of artifact to determine the base objects of the created context.
   * @param componentBuildingDefinitionRegistryFactory
   * @since 3.7.0
   */
  public ZernovaArtifactContext(ZernovaContext muleContext, ArtifactAst artifactAst,
                             Optional<ConfigurationProperties> parentConfigurationProperties,
                             BaseConfigurationComponentLocator baseConfigurationComponentLocator,
                             ContributedErrorTypeRepository errorTypeRepository,
                             ContributedErrorTypeLocator errorTypeLocator,
                             Map<String, String> artifactProperties,
                             ArtifactType artifactType,
                             ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry,
                             MemoryManagementService memoryManagementService,
                             FeatureFlaggingService featureFlaggingService,
                             ExpressionLanguageMetadataService expressionLanguageMetadataService) {
    this.muleContext = (ZernovaContextWithRegistry) muleContext;
    this.featureFlaggingService = featureFlaggingService;
    this.expressionLanguageMetadataService = expressionLanguageMetadataService;
    this.coreFunctionsProvider = this.muleContext.getRegistry().get(CORE_FUNCTIONS_PROVIDER_REGISTRY_KEY);
    this.artifactType = artifactType;
    this.serviceDiscoverer = new DefaultRegistry(muleContext);
    this.resourceLocator = new DefaultResourceLocator();
    this.baseComponentLocator = baseConfigurationComponentLocator;
    this.errorTypeRepository = errorTypeRepository;
    this.errorTypeLocator = errorTypeLocator;
    this.artifactProperties = artifactProperties;
    this.memoryManagementService = memoryManagementService;

    extensionManager = muleContext.getExtensionManager();

    componentLocator = new SpringConfigurationComponentLocator(componentName -> {
      try {
        BeanDefinition beanDefinition = getBeanFactory().getBeanDefinition(componentName);
        return beanDefinition.isPrototype();
      } catch (NoSuchBeanDefinitionException e) {
        return false;
      }
    });

    // TODO (MULE-19608) remove this and make it into a component building definition
    this.beanDefinitionFactory =
        new BeanDefinitionFactory(muleContext.getConfiguration().getId(),
                                  componentBuildingDefinitionRegistry,
                                  featureFlaggingService.isEnabled(DISABLE_ATTRIBUTE_PARAMETER_WHITESPACE_TRIMMING),
                                  featureFlaggingService.isEnabled(DISABLE_POJO_TEXT_CDATA_WHITESPACE_TRIMMING));

    // TODO W-10855416: remove this
    this.validateAppModelWithRegionClassloader =
        featureFlaggingService.isEnabled(VALIDATE_APPLICATION_MODEL_WITH_REGION_CLASSLOADER);

    this.applicationModel = artifactAst;

    // TODO MULE-18786 create the providers that depend on the AST only, and for the rest delegate on the resolver from the base
    // context
    this.configurationProperties = createConfigurationAttributeResolver(applicationModel, parentConfigurationProperties,
                                                                        artifactProperties,
                                                                        new ClassLoaderResourceProvider(muleContext
                                                                            .getExecutionClassLoader()),
                                                                        of(featureFlaggingService));

    try {
      initialiseIfNeeded(configurationProperties.getConfigurationPropertiesResolver());
      applicationModel.updatePropertiesResolver(configurationProperties.getConfigurationPropertiesResolver());

      validateArtifact(applicationModel);
    } catch (ConfigurationException | InitialisationException e) {
      throw new ZernovaRuntimeException(e);
    }
    registerErrors(applicationModel);
    registerApplicationExtensionModel();
  }

  protected ZernovaRegistry getMuleRegistry() {
    return this.muleContext.getRegistry();
  }

  protected void validateArtifact(final ArtifactAst artifactAst) throws ConfigurationException {
    doValidateModel(artifactAst, v -> true);
  }

  protected final void doValidateModel(ArtifactAst appModel, Predicate<Validation> validationsFilter)
      throws ConfigurationException {

    final ValidationResult validation = validatorBuilder()
        .withValidationEnricher(new RuntimeValidationEnricher(new ReusableArtifactAstDependencyGraphProvider(appModel),
                                                              muleContext))
        .withValidationsFilter(validationsFilter)
        // get the region classloader from the artifact one
        .withArtifactRegionClassLoader(getValidationClassloader())
        .build()
        .validate(appModel);

    logWarningsAndThrowIfContainsErrors(validation, LOGGER);
  }

  // TODO W-10855416: remove this and only validate it with the region classloader
  private ClassLoader getValidationClassloader() {
    if (validateAppModelWithRegionClassloader) {
      return muleContext.getExecutionClassLoader().getParent();
    }
    return muleContext.getExecutionClassLoader();
  }


  private ClassLoader getRegionClassLoader() {
    return muleContext.getExecutionClassLoader().getParent();
  }

  protected void registerApplicationExtensionModel() {
    if (!artifactType.equals(APP)) {
      return;
    }

    final String appName = muleContext.getConfiguration().getId();

    if (extensionManager.getExtension(appName).isPresent()) {
      logModelNotGenerated("ExtensionModel already registered");
      return;
    }

    fetchOrGenerateApplicationExtensionModel(appName).ifPresent(appExtensionModel -> {
      ExtensionManager appManager = extensionManager instanceof CompositeArtifactExtensionManager composite
          ? composite.getChildExtensionManager()
          : extensionManager;

      appManager.registerExtension(appExtensionModel);
    });
  }

  private Optional<ExtensionModel> fetchOrGenerateApplicationExtensionModel(String appName) {
    Optional<ExtensionModel> appExtensionModel = applicationModel.dependencies().stream()
        .filter(em -> em.getName().equals(appName))
        .findFirst();

    if (appExtensionModel.isPresent()) {
      return appExtensionModel;
    }

    try {
      return parseArtifactExtensionModel(applicationModel,
                                         getRegionClassLoader(),
                                         muleContext.getConfiguration(),
                                         muleContext.getExtensionManager(),
                                         expressionLanguageMetadataService);
    } catch (ConfigurationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  private void logModelNotGenerated(String reason) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}. ExtensionModel for app {} not generated", reason, muleContext.getConfiguration().getId());
    }
  }

  protected void registerErrors(final ArtifactAst artifactAst) {
    doRegisterErrors(artifactAst);
  }

  protected void doRegisterErrors(final ArtifactAst artifactAst) {
    final ErrorTypeRepository repository = artifactAst.enrichedErrorTypeRepository();
    final ErrorTypeLocator errorTypeLocator =
        createDefaultErrorTypeLocator(repository, ofNullable(featureFlaggingService));

    registerErrorMappings(repository, errorTypeLocator,
                          artifactAst.dependencies(), artifactAst::dependenciesDsl);

    // Because instances of the repository and locator may be already created and injected into another objects, those instances
    // cannot just be set into the registry, and this contributing layer is needed to ensure the correct functioning of the DI
    // mechanism.
    this.errorTypeRepository.setDelegate(repository);
    this.errorTypeLocator.setDelegate(errorTypeLocator);
  }

  public void initialize() {
    applicationModel =
        prepareAstForRuntime(applicationModel, applicationModel.dependencies(), ofNullable(featureFlaggingService));
  }

  @Override
  protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    super.prepareBeanFactory(beanFactory);
    beanFactory.setBeanExpressionResolver(null);

    registerEditors(beanFactory);

    registerAnnotationConfigProcessors(beanFactory);

    addBeanPostProcessors(beanFactory,
                          new ZernovaContextPostProcessor(muleContext),
                          new ComponentLocatorCreatePostProcessor(componentLocator));

    beanFactory.registerSingleton(OBJECT_MULE_CONTEXT, muleContext);

    prepareObjectProviders(objectProviders);
  }

  /**
   * Configures the given {@link ConfigurableObjectProvider}s.
   *
   * @param configurableObjectProviders The {@link ConfigurableObjectProvider}s to configure.
   */
  protected void prepareObjectProviders(List<ConfigurableObjectProvider> configurableObjectProviders) {
    // If the object providers list is empty we don't even bother creating the ObjectProviderConfiguration
    // (this may happen during lazy initialization for example)
    if (configurableObjectProviders.isEmpty()) {
      return;
    }

    ZernovaArtifactObjectProvider muleArtifactObjectProvider = new ZernovaArtifactObjectProvider(this);
    ImmutableObjectProviderConfiguration providerConfiguration =
        new ImmutableObjectProviderConfiguration(configurationProperties, muleArtifactObjectProvider);
    for (ConfigurableObjectProvider objectProvider : configurableObjectProviders) {
      objectProvider.configure(providerConfiguration);
    }
  }

  /**
   * Process all the {@link ObjectProvider}s from the {@link ApplicationModel} to get their beans and register them inside the
   * spring bean factory so they can be used for dependency injection.
   *
   * @param beanFactory the spring bean factory where the objects will be registered.
   */
  protected void registerObjectFromObjectProviders(ConfigurableListableBeanFactory beanFactory) {
    ((ObjectProviderAwareBeanFactory) beanFactory).setObjectProviders(objectProviders);
  }

  private List<Pair<ComponentAst, Optional<String>>> lookObjectProvidersComponentModels(ArtifactAst applicationModel,
                                                                                        Map<ComponentAst, SpringComponentModel> springComponentModels) {
    return applicationModel.topLevelComponentsStream()
        .filter(componentModel -> {
          final SpringComponentModel springModel = springComponentModels.get(componentModel);
          return springModel != null && springModel.getType() != null
              && ConfigurableObjectProvider.class.isAssignableFrom(springModel.getType());
        })
        .map(componentModel -> new Pair<>(componentModel, componentModel.getComponentId()))
        .toList();
  }

  private void registerEditors(ConfigurableListableBeanFactory beanFactory) {
    beanFactory.addPropertyEditorRegistrar(new ZernovaPropertyEditorRegistrar());
  }

  protected void addBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, BeanPostProcessor... processors) {
    for (BeanPostProcessor processor : processors) {
      beanFactory.addBeanPostProcessor(processor);
    }
  }

  @Override
  public void close() {
    if (isRunning()) {
      try {
        super.close();
      } catch (Exception e) {
        for (ObjectProvider objectProvider : objectProviders) {
          disposeIfNeeded(objectProvider, LOGGER);
        }
        throw new ZernovaRuntimeException(e);
      }
      disposeIfNeeded(configurationProperties.getConfigurationPropertiesResolver(), LOGGER);
      // Additional Spring cache cleanup
      clearSpringSoftReferencesCachesForDynamicClassLoaders();
      try {
        ByteBuddySpringCachesManager.clearCaches();
        // Needed due to a shade of spring-core in the extensions-support module.
        IntrospectionUtils.resetCommonCaches();
      } catch (Exception e) {
        LOGGER.warn("Spring caches cleanup failed", e);
      }
      beanDefinitionFactory.close();
    }
  }

  /**
   * We use ByteBuddy to enhance classes defined with the Java SDK, in order to make them implement the {@link Component}
   * interface. The classloader used to load such dynamic classes is being hold by a cache in Spring, and that cache can be
   * cleared by calling {@link CachedIntrospectionResults#clearClassLoader}. Notice that this method can be called with the
   * classloader of the class itself, or any of the parents in its hierarchy.
   *
   * @see AnnotatedObjectInvocationHandler#addAnnotationsToClass
   * @see CachedIntrospectionResults#clearClassLoader
   */
  private void clearSpringSoftReferencesCachesForDynamicClassLoaders() {
    ClassLoader regionClassLoader = getRegionClassLoader();
    if (!(regionClassLoader instanceof RegionClassLoader region)) {
      // The method #getRegionClassLoader() should always return the corresponding RegionClassLoader. However, in the
      // integration tests (which is an ArtifactFunctionalTestCase), the classloader here is an instance of
      // TestRegionClassLoader. That class extends RegionClassLoader, but it's loaded with a different classloader, and
      // then we would be getting a ClassCastException here. That's the only reason for this early-return.
      LOGGER.debug("Got an instance of '{}' as region classloader. We can't clean the spring soft-references caches.",
                   regionClassLoader.getClass().getCanonicalName());
      return;
    }
    clearClassLoader(region.getClassLoader());
    for (ArtifactClassLoader pluginClassLoader : region.getArtifactPluginClassLoaders()) {
      if (pluginClassLoader instanceof WithAttachedClassLoaders withAttachedClassLoaders) {
        for (ClassLoader dynamicClassLoader : withAttachedClassLoaders.getAttachedClassLoaders()) {
          clearClassLoader(dynamicClassLoader);
        }
      }
    }
  }

  @Override
  protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
    createApplicationComponents(beanFactory, applicationModel, true);
  }

  /**
   * @param componentAst The {@link ComponentAst} to test.
   * @return if the {@code componentAst} needs to be always enabled.
   */
  protected static boolean isAlwaysEnabledComponent(ComponentAst componentAst) {
    return componentAst.getModel(HasStereotypeModel.class)
        .map(stm -> stm.getStereotype() != null && stm.getStereotype().isAssignableTo(APP_CONFIG))
        .orElse(false);
  }

  /**
   * Callback to perform actions when a new configurable object provider is discovered as part of the creation of components.
   * <p>
   * Derived classes must call the super class's implementation of this method.
   *
   * @param objectProvider The {@link ConfigurableObjectProvider} just discovered.
   */
  protected void onObjectProviderDiscovered(ConfigurableObjectProvider objectProvider) {
    objectProviders.add(objectProvider);
  }

  /**
   * Creates the definition for all the objects to be created form the enabled components in the {@code applicationModel}.
   *
   * @param beanFactory      the bean factory in which definition must be created.
   * @param applicationModel the artifact application model.
   * @param mustBeRoot       if the component must be root to be created.
   * @return an order list of the created bean names. The order must be respected for the creation of the objects.
   */
  protected List<Pair<String, ComponentAst>> createApplicationComponents(DefaultListableBeanFactory beanFactory,
                                                                         ArtifactAst applicationModel,
                                                                         boolean mustBeRoot) {
    Map<ComponentAst, SpringComponentModel> springComponentModels = new LinkedHashMap<>();

    return doCreateApplicationComponents(beanFactory, applicationModel, mustBeRoot, springComponentModels);
  }

  protected List<Pair<String, ComponentAst>> doCreateApplicationComponents(DefaultListableBeanFactory beanFactory,
                                                                           ArtifactAst applicationModel, boolean mustBeRoot,
                                                                           Map<ComponentAst, SpringComponentModel> springComponentModels) {
    Set<String> alwaysEnabledTopLevelComponents = new HashSet<>();
    Set<ComponentIdentifier> alwaysEnabledUnnamedTopLevelComponents = applicationModel.topLevelComponentsStream()
        .filter(ZernovaArtifactContext::isAlwaysEnabledComponent)
        .peek(cm -> cm.getComponentId().ifPresent(alwaysEnabledTopLevelComponents::add))
        .filter(cm -> !cm.getComponentId().isPresent())
        .map(ComponentAst::getIdentifier)
        .collect(toSet());
    Set<String> alwaysEnabledGeneratedTopLevelComponentsName = new HashSet<>();

    List<Pair<String, ComponentAst>> createdComponentModels = new ArrayList<>();

    Set<ComponentAst> rootComponents = resolveRootComponents(applicationModel);

    recursiveStreamWithHierarchy(applicationModel, BOTTOM_UP, true)
        // Create component if must not be root is mandatory or component is a root component or component is child of a root
        // component
        .filter(cm -> !mustBeRoot || rootComponents.contains(cm.getFirst())
            || cm.getSecond().stream().anyMatch(rootComponents::contains))
        .filter(cm -> !isIgnored(cm.getFirst()))
        .forEach(cm -> {
          if (rootComponents.contains(cm.getFirst())) {
            cm.getFirst().getComponentId()
                .ifPresent(componentName -> createdComponentModels.add(new Pair<>(componentName, cm.getFirst())));
          }

          beanDefinitionFactory.resolveComponent(springComponentModels,
                                                 cm.getSecond(),
                                                 cm.getFirst(), beanFactory, componentLocator);

          if (rootComponents.contains(cm.getFirst())) {
            componentLocator.addComponentLocation(cm.getFirst().getLocation());
          }
        });


    springComponentModels.values().stream()
        .filter(resolvedComponentModel -> rootComponents.contains(resolvedComponentModel.getComponent()))
        .forEach(resolvedComponentModel -> registerRootSpringBean(beanFactory, alwaysEnabledUnnamedTopLevelComponents,
                                                                  alwaysEnabledGeneratedTopLevelComponentsName,
                                                                  createdComponentModels, resolvedComponentModel));

    // This should only be done once at the initial application model creation, called from Spring
    List<Pair<ComponentAst, Optional<String>>> objectProvidersByName =
        lookObjectProvidersComponentModels(applicationModel, springComponentModels);

    objectProvidersByName.stream()
        .map(pair -> springComponentModels.get(pair.getFirst()).getObjectInstance())
        .forEach(this::onObjectProviderDiscovered);
    registerObjectFromObjectProviders(beanFactory);

    Set<String> objectProviderNames = objectProvidersByName.stream()
        .map(Pair::getSecond)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toSet());

    // Put object providers first, then always enabled components, then the rest
    createdComponentModels.sort(comparing(beanNameAndComponent -> {
      final String beanName = beanNameAndComponent.getFirst();
      if (objectProviderNames.contains(beanName)) {
        return 1;
      } else if (alwaysEnabledGeneratedTopLevelComponentsName.contains(beanName)) {
        return 2;
      } else if (alwaysEnabledTopLevelComponents.contains(beanName)) {
        return 3;
      } else {
        return 4;
      }
    }));

    return createdComponentModels;
  }

  protected Set<ComponentAst> resolveRootComponents(ArtifactAst applicationModel) {
    Set<ComponentAst> rootComponents = new HashSet<>(applicationModel.topLevelComponents());
    for (ApplicationModelAstPostProcessor astPostProcessor : AST_POST_PROCESSORS.get()) {
      rootComponents = astPostProcessor.resolveRootComponents(rootComponents, extensionManager.getExtensions());
    }
    return rootComponents;
  }

  protected boolean isIgnored(ComponentAst componentAst) {
    return beanDefinitionFactory.isComponentIgnored(componentAst.getIdentifier());
  }

  private void registerRootSpringBean(DefaultListableBeanFactory beanFactory,
                                      Set<ComponentIdentifier> alwaysEnabledUnnamedTopLevelComponents,
                                      Set<String> alwaysEnabledGeneratedTopLevelComponentsName,
                                      List<Pair<String, ComponentAst>> createdComponentModels,
                                      SpringComponentModel resolvedComponentModel) {
    String nameAttribute = resolvedComponentModel.getComponent().getComponentId()
        .orElse(resolvedComponentModel.getComponentName());
    if (nameAttribute == null) {
      // This may be a configuration that does not requires a name.
      nameAttribute = uniqueValue(resolvedComponentModel.getBeanDefinition().getBeanClassName());

      if (alwaysEnabledUnnamedTopLevelComponents.contains(resolvedComponentModel.getComponent().getIdentifier())) {
        alwaysEnabledGeneratedTopLevelComponentsName.add(nameAttribute);
        createdComponentModels.add(new Pair<>(nameAttribute, resolvedComponentModel.getComponent()));
      } else if (resolvedComponentModel.getType() != null
          && TransactionManagerFactory.class.isAssignableFrom(resolvedComponentModel.getType())) {
        createdComponentModels.add(new Pair<>(nameAttribute, resolvedComponentModel.getComponent()));
      }
    }

    beanFactory.registerBeanDefinition(nameAttribute,
                                       requireNonNull(resolvedComponentModel.getBeanDefinition(),
                                                      "BeanDefinition null for "
                                                          + resolvedComponentModel.getComponent().toString()));
    postProcessBeanDefinition(resolvedComponentModel, beanFactory, nameAttribute);
  }

  @Override
  protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
    super.customizeBeanFactory(beanFactory);
    baseComponentLocator.setDelegate(componentLocator);
    createServiceConfigurator(beanFactory).createArtifactServices();
  }

  protected SpringZernovaContextServiceConfigurator createServiceConfigurator(DefaultListableBeanFactory beanFactory) {
    return new SpringZernovaContextServiceConfigurator(muleContext,
                                                    getCoreFunctionsProvider(),
                                                    getConfigurationProperties(),
                                                    artifactProperties,
                                                    getArtifactType(),
                                                    getApplicationModel(),
                                                    beanFactory,
                                                    getServiceDiscoverer(),
                                                    getResourceLocator(),
                                                    memoryManagementService);
  }

  @Override
  protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;

    if (!findComponentDefinitionModel(applicationModel, NOTIFICATIONS_IDENTIFIER).isPresent()) {
      registerNotificationManagerBean(beanDefinitionRegistry);
    }
  }

  private void registerNotificationManagerBean(BeanDefinitionRegistry beanDefinitionRegistry) {
    beanDefinitionRegistry
        .registerBeanDefinition(OBJECT_NOTIFICATION_MANAGER,
                                getBeanDefinitionBuilder(ServerNotificationManagerConfigurator.class)
                                    .addPropertyValue("enabledNotifications", ImmutableList
                                        .<NotificationConfig<? extends Notification, ? extends NotificationListener>>builder()
                                        .add(new EnabledNotificationConfig<>(ZernovaContextNotificationListener.class,
                                                                             ZernovaContextNotification.class))
                                        .add(new EnabledNotificationConfig<>(SecurityNotificationListener.class,
                                                                             SecurityNotification.class))
                                        .add(new EnabledNotificationConfig<>(ManagementNotificationListener.class,
                                                                             ManagementNotification.class))
                                        .add(new EnabledNotificationConfig<>(ConnectionNotificationListener.class,
                                                                             ConnectionNotification.class))
                                        .add(new EnabledNotificationConfig<>(CustomNotificationListener.class,
                                                                             CustomNotification.class))
                                        .add(new EnabledNotificationConfig<>(ExceptionNotificationListener.class,
                                                                             ExceptionNotification.class))
                                        .add(new EnabledNotificationConfig<>(TransactionNotificationListener.class,
                                                                             TransactionNotification.class))
                                        .add(new EnabledNotificationConfig<>(ExtensionNotificationListener.class,
                                                                             ExtensionNotification.class))
                                        .build())
                                    .getBeanDefinition());
  }

  private void registerAnnotationConfigProcessors(ConfigurableListableBeanFactory beanFactory) {
    registerInjectorProcessor(beanFactory);
  }

  protected void registerInjectorProcessor(ConfigurableListableBeanFactory beanFactory) {
    ZernovaInjectorProcessor muleInjectorProcessor = null;
    if (artifactType.equals(APP) || artifactType.equals(POLICY) || artifactType.equals(DOMAIN)) {
      muleInjectorProcessor = new ZernovaInjectorProcessor();
    }
    if (muleInjectorProcessor != null) {
      muleInjectorProcessor.setBeanFactory(beanFactory);
      beanFactory.addBeanPostProcessor(muleInjectorProcessor);
    }
  }

  @Override
  protected DefaultListableBeanFactory createBeanFactory() {
    // Copy all postProcessors defined in the defaultMuleConfig so that they get applied to the child container
    DefaultListableBeanFactory beanFactory = new ObjectProviderAwareBeanFactory(getInternalParentBeanFactory());
    beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());

    return beanFactory;
  }

  /**
   * {@inheritDoc} This implementation returns {@code false} if the context hasn't been initialised yet, in opposition to the
   * default implementation which throws an exception
   */
  @Override
  public boolean isRunning() {
    try {
      return super.isRunning();
    } catch (IllegalStateException e) {
      return false;
    }
  }

  /**
   * Forces the registration of instances of {@link TransformerResolver} and {@link Converter} to be created, so that they are
   * added to the transformation graph.
   */
  protected static void postProcessBeanDefinition(SpringComponentModel resolvedComponent, BeanDefinitionRegistry registry,
                                                  String beanName) {
    if (Converter.class.isAssignableFrom(resolvedComponent.getType())) {
      GenericBeanDefinition converterBeanDefinitionCopy = new GenericBeanDefinition(resolvedComponent.getBeanDefinition());
      converterBeanDefinitionCopy.setScope(SPRING_SINGLETON_OBJECT);
      registry.registerBeanDefinition(beanName + "-" + "converter", converterBeanDefinitionCopy);
    }
  }

  public ZernovaContextWithRegistry getMuleContext() {
    return muleContext;
  }

  protected ZernovaFunctionsBindingContextProvider getCoreFunctionsProvider() {
    return coreFunctionsProvider;
  }

  protected PropertiesResolverConfigurationProperties getConfigurationProperties() {
    return configurationProperties;
  }

  protected ArtifactType getArtifactType() {
    return artifactType;
  }

  public Registry getRegistry() {
    return getMuleContext().getRegistry().get(OBJECT_REGISTRY);
  }

  @Override
  public String toString() {
    return format("%s: %s (%s)", this.getClass().getName(), muleContext.getConfiguration().getId(), artifactType.name());
  }

  public ArtifactAst getApplicationModel() {
    return applicationModel;
  }

  protected DefaultRegistry getServiceDiscoverer() {
    return serviceDiscoverer;
  }

  protected DefaultResourceLocator getResourceLocator() {
    return resourceLocator;
  }
}
