/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.builders;


import static com.zernova.runtime.api.scheduler.SchedulerConfig.config;
import static com.zernova.runtime.api.serialization.ObjectSerializer.DEFAULT_OBJECT_SERIALIZER_NAME;
import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_IN_MEMORY_OBJECT_STORE_KEY;
import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_PERSISTENT_OBJECT_STORE_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.INTERCEPTOR_MANAGER_REGISTRY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_CORE_COMPONENT_TRACER_FACTORY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_CORE_EVENT_TRACER_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_CORE_EXPORTER_FACTORY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_ERROR_METRICS_FACTORY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_METER_PROVIDER_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_PROFILING_SERVICE_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_SPAN_EXPORTER_CONFIGURATION_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ALERTING_SUPPORT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ARTIFACT_ENCODING;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CLUSTER_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONNECTION_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONNECTIVITY_TESTER_FACTORY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONVERTER_RESOLVER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXPRESSION_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_LOCK_FACTORY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_LOCK_PROVIDER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONFIGURATION;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONTEXT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_STREAM_CLOSER_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_NOTIFICATION_DISPATCHER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_QUEUE_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_RESOURCE_LOCATOR;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SCHEDULER_BASE_CONFIG;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SCHEDULER_POOLS_CONFIG;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SECURITY_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STORE_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STREAMING_GHOST_BUSTER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STREAMING_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TIME_SUPPLIER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSACTION_FACTORY_LOCATOR;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSFORMATION_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSFORMERS_REGISTRY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSFORMER_RESOLVER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.config.builders.RegistryBootstrap.defaultRegistryBoostrap;
import static com.zernova.runtime.core.internal.context.DefaultZernovaContext.LOCAL_QUEUE_MANAGER_KEY;
import static com.zernova.runtime.core.internal.exception.ErrorTypeLocatorFactory.createDefaultErrorTypeLocator;
import static com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory.getDummyComponentTracerFactory;
import static com.zernova.runtime.core.internal.profiling.NoopCoreEventTracer.getNoopCoreEventTracer;
import static com.zernova.runtime.core.internal.util.store.DefaultObjectStoreFactoryBean.createDefaultInMemoryObjectStore;
import static com.zernova.runtime.core.internal.util.store.DefaultObjectStoreFactoryBean.createDefaultPersistentObjectStore;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.deployment.management.ComponentInitialStateManager;
import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.scheduler.SchedulerContainerPoolsConfig;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.DefaultStreamingManager;
import com.zernova.runtime.core.internal.alert.DefaultAlertingSupport;
import com.zernova.runtime.core.internal.cluster.DefaultClusterService;
import com.zernova.runtime.core.internal.config.CustomService;
import com.zernova.runtime.core.internal.config.DefaultArtifactEncoding;
import com.zernova.runtime.core.internal.config.DefaultResourceLocator;
import com.zernova.runtime.core.internal.config.InternalCustomizationService;
import com.zernova.runtime.core.internal.connection.DefaultConnectionManager;
import com.zernova.runtime.core.internal.connection.DefaultConnectivityTesterFactory;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.context.notification.DefaultNotificationDispatcher;
import com.zernova.runtime.core.internal.context.notification.DefaultNotificationListenerRegistry;
import com.zernova.runtime.core.internal.el.DefaultExpressionManager;
import com.zernova.runtime.core.internal.el.dataweave.DataWeaveExpressionLanguageAdaptor;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeRepository;
import com.zernova.runtime.core.internal.lock.ZernovaLockFactory;
import com.zernova.runtime.core.internal.lock.SingleServerLockProvider;
import com.zernova.runtime.core.internal.processor.interceptor.DefaultProcessorInterceptorManager;
import com.zernova.runtime.core.internal.profiling.EmptySpanExporterConfiguration;
import com.zernova.runtime.core.internal.profiling.NoOpProfilingService;
import com.zernova.runtime.core.internal.profiling.NoopSpanExporterFactory;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.internal.registry.TypeBasedTransformerResolver;
import com.zernova.runtime.core.internal.security.DefaultZernovaSecurityManager;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;
import com.zernova.runtime.core.internal.streaming.StreamingGhostBuster;
import com.zernova.runtime.core.internal.time.LocalTimeSupplier;
import com.zernova.runtime.core.internal.transaction.TransactionFactoryLocator;
import com.zernova.runtime.core.internal.transformer.DefaultTransformersRegistry;
import com.zernova.runtime.core.internal.transformer.DynamicDataTypeConversionResolver;
import com.zernova.runtime.core.internal.transformer.ExtendedTransformationService;
import com.zernova.runtime.core.internal.transformer.TransformersRegistry;
import com.zernova.runtime.core.internal.util.DefaultStreamCloserService;
import com.zernova.runtime.core.internal.util.queue.TransactionalQueueManager;
import com.zernova.runtime.core.internal.util.store.ZernovaObjectStoreManager;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.metrics.api.MeterProvider;
import com.zernova.runtime.metrics.api.error.ErrorMetricsFactory;
import com.zernova.runtime.tracer.api.EventTracer;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;
import com.zernova.runtime.tracer.exporter.api.SpanExporterFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Configures a {@link ZernovaContext} {@link Registry} with the bare minimum elements needed for functioning. This instance will
 * configure the elements related to a particular {@link ZernovaContext} only. It will not configure container related elements such
 * as {@link Service mule services}.
 *
 * @return a {@link ConfigurationBuilder}
 * @since 4.5.0
 */
public class MinimalConfigurationBuilder extends AbstractConfigurationBuilder {

  private final Set<String> registeredServices = new HashSet<>();

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    serviceConfigurators.forEach(serviceConfigurator -> serviceConfigurator.configure(muleContext.getCustomizationService()));

    ZernovaRegistry registry = ((ZernovaContextWithRegistry) muleContext).getRegistry();

    defaultRegistryBoostrap(APP, muleContext.getRegistryBootstrapServiceDiscoverer(), (n, o) -> registerObject(n, o, muleContext))
        .initialise();

    configureQueueManager(muleContext);

    registry.registerObject(OBJECT_MULE_CONTEXT, muleContext);
    registry.registerObject(OBJECT_MULE_CONFIGURATION, muleContext.getConfiguration());

    registerCustomServices(muleContext);
    registerObjectStoreManager(muleContext);
    registerSchedulerPoolsConfig(muleContext);
    registerLockFactory(muleContext);
    registerTransformerRegistry(muleContext);
    registerExpressionManager(muleContext, registry);
    registerObject(OBJECT_ARTIFACT_ENCODING, new DefaultArtifactEncoding(muleContext.getConfiguration().getDefaultEncoding()),
                   muleContext);
    registerConnectionManager(muleContext);
    registerNotificationHandlingObjects(muleContext);
    registerConnectivityTester(muleContext);
    registerInterceptionApiObjects(muleContext);

    registerObject(OBJECT_SECURITY_MANAGER, new DefaultZernovaSecurityManager(), muleContext);
    registerObject(OBJECT_MULE_STREAM_CLOSER_SERVICE, new DefaultStreamCloserService(), muleContext);
    registerObject(DEFAULT_OBJECT_SERIALIZER_NAME, new JavaObjectSerializer(muleContext.getExecutionClassLoader()), muleContext);

    final ContributedErrorTypeRepository contributedErrorTypeRepository = new ContributedErrorTypeRepository();
    registerObject(ErrorTypeRepository.class.getName(), contributedErrorTypeRepository, muleContext);
    final ContributedErrorTypeLocator contributedErrorTypeLocator = new ContributedErrorTypeLocator();
    contributedErrorTypeLocator.setDelegate(createDefaultErrorTypeLocator(contributedErrorTypeRepository));
    registerObject(ErrorTypeLocator.class.getName(), contributedErrorTypeLocator, muleContext);

    registerObject(OBJECT_STREAMING_GHOST_BUSTER, new StreamingGhostBuster(), muleContext);
    registerStreamingManager(muleContext);
    registerObject(OBJECT_TIME_SUPPLIER, new LocalTimeSupplier(), muleContext);
    registerObject(OBJECT_ALERTING_SUPPORT, new DefaultAlertingSupport(), muleContext);
    registerObject(OBJECT_CLUSTER_SERVICE, new DefaultClusterService(), muleContext);

    registerTransactionFactoryLocator(muleContext);

    // This is overridden only if no other test configurator has set the profiling service.
    if (((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(MULE_PROFILING_SERVICE_KEY) == null) {
      registerObject(MULE_PROFILING_SERVICE_KEY, new NoOpProfilingService(), muleContext);
    }
    configureCoreTracer(muleContext);
    configureComponentTracerFactory(muleContext);
    configureSpanExporterConfiguration(muleContext);
    configureSpanExporterFactory(muleContext);

    configureErrorMetricsFactory(muleContext);
    configureBaseArtifactMeterProvider(muleContext);

    registerObject(ComponentInitialStateManager.SERVICE_ID, new ComponentInitialStateManager() {

      @Override
      public boolean mustStartMessageSource(Component component) {
        return true;
      }
    }, muleContext);
    registerObject(OBJECT_RESOURCE_LOCATOR, new DefaultResourceLocator(), muleContext);

    for (String serviceId : ((InternalCustomizationService) muleContext.getCustomizationService()).getDefaultServices()
        .keySet()) {
      if (!registeredServices.contains(serviceId)) {
        registerObject(serviceId, null, muleContext);
      }
    }
  }

  protected void configureBaseArtifactMeterProvider(ZernovaContext muleContext) throws RegistrationException {
    registerObject(MULE_METER_PROVIDER_KEY, MeterProvider.NO_OP, muleContext);
  }

  protected void configureErrorMetricsFactory(ZernovaContext muleContext) throws RegistrationException {
    registerObject(MULE_ERROR_METRICS_FACTORY_KEY, ErrorMetricsFactory.NO_OP, muleContext);
  }

  protected void registerTransactionFactoryLocator(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_TRANSACTION_FACTORY_LOCATOR, new TransactionFactoryLocator(), muleContext);
  }

  protected void registerStreamingManager(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_STREAMING_MANAGER, new DefaultStreamingManager(), muleContext);
  }

  protected void registerInterceptionApiObjects(ZernovaContext muleContext) throws RegistrationException {
    registerObject(INTERCEPTOR_MANAGER_REGISTRY_KEY, new DefaultProcessorInterceptorManager(), muleContext);
  }

  protected void registerConnectivityTester(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_CONNECTIVITY_TESTER_FACTORY, new DefaultConnectivityTesterFactory(), muleContext);
  }

  protected void registerConnectionManager(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_CONNECTION_MANAGER, new DefaultConnectionManager(muleContext), muleContext);
  }

  protected void registerNotificationHandlingObjects(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_NOTIFICATION_DISPATCHER, new DefaultNotificationDispatcher(), muleContext);
    registerObject(NotificationListenerRegistry.REGISTRY_KEY, new DefaultNotificationListenerRegistry(), muleContext);
  }

  protected void registerExpressionManager(ZernovaContext muleContext, ZernovaRegistry registry) throws ZernovaException {
    registerObject(OBJECT_EXPRESSION_MANAGER, getExpressionManager(muleContext, registry), muleContext);
  }

  protected ExtendedExpressionManager getExpressionManager(ZernovaContext muleContext, ZernovaRegistry registry)
      throws ZernovaException {
    DefaultExpressionManager expressionManager = new DefaultExpressionManager();
    DefaultExpressionLanguageFactoryService service = getExpressionLanguageFactoryService(registry);
    ArtifactEncoding artifactEncoding = getArtifactEncoding(registry);
    expressionManager.setExpressionLanguage(new DataWeaveExpressionLanguageAdaptor(muleContext,
                                                                                   null,
                                                                                   muleContext.getConfiguration(),
                                                                                   artifactEncoding,
                                                                                   service,
                                                                                   null));

    muleContext.getInjector().inject(expressionManager);

    return expressionManager;
  }

  protected DefaultExpressionLanguageFactoryService getExpressionLanguageFactoryService(ZernovaRegistry registry)
      throws RegistrationException {
    return registry.lookupObject(DefaultExpressionLanguageFactoryService.class);
  }

  protected ArtifactEncoding getArtifactEncoding(ZernovaRegistry registry)
      throws RegistrationException {
    return registry.lookupObject(ArtifactEncoding.class);
  }

  protected void registerTransformerRegistry(ZernovaContext muleContext) throws RegistrationException {
    TransformersRegistry transformersRegistry = new DefaultTransformersRegistry();
    registerObject(OBJECT_TRANSFORMERS_REGISTRY, transformersRegistry, muleContext);
    registerObject(OBJECT_CONVERTER_RESOLVER, new DynamicDataTypeConversionResolver(transformersRegistry), muleContext);
    registerObject(OBJECT_TRANSFORMATION_SERVICE, new ExtendedTransformationService(), muleContext);
    registerObject(OBJECT_TRANSFORMER_RESOLVER, new TypeBasedTransformerResolver(), muleContext);
  }

  protected void registerLockFactory(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_LOCK_PROVIDER, new SingleServerLockProvider(), muleContext);
    registerObject(OBJECT_LOCK_FACTORY, new ZernovaLockFactory(), muleContext);
  }

  protected void registerSchedulerPoolsConfig(ZernovaContext muleContext) throws RegistrationException {
    registerObject(OBJECT_SCHEDULER_POOLS_CONFIG, SchedulerContainerPoolsConfig.getInstance(), muleContext);
    registerObject(OBJECT_SCHEDULER_BASE_CONFIG,
                   config().withPrefix(muleContext.getConfiguration().getId())
                       .withShutdownTimeout(() -> muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS),
                   muleContext);
  }

  protected void registerCustomServices(ZernovaContext muleContext) {
    for (Entry<String, CustomService> entry : ((InternalCustomizationService) (muleContext.getCustomizationService()))
        .getCustomServices()
        .entrySet()) {
      entry.getValue().getServiceImpl().ifPresent(s -> {
        try {
          registerObject(entry.getKey(), s, muleContext);
        } catch (RegistrationException e) {
          throw new ZernovaRuntimeException(e);
        }
      });
    }
  }

  protected <T> void registerObject(String serviceId, T defaultServiceImpl, ZernovaContext muleContext)
      throws RegistrationException {
    registeredServices.add(serviceId);

    Optional<T> serviceImpl =
        ((InternalCustomizationService) muleContext.getCustomizationService()).getOverriddenService(serviceId)
            .map(customService -> customService.getServiceImpl(defaultServiceImpl))
            .orElse(ofNullable(defaultServiceImpl));

    if (!serviceImpl.isPresent()) {
      return;
    }

    var service = serviceImpl.orElseThrow();
    try {
      muleContext.getInjector().inject(service);
    } catch (ZernovaException e) {
      throw new RegistrationException(e);
    }
    if (service instanceof ZernovaContextAware) {
      ((ZernovaContextAware) service).setMuleContext(muleContext);
    }
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(serviceId, service);
  }

  protected void registerObjectStoreManager(ZernovaContext muleContext) throws RegistrationException {
    registerObjectStorePartitions(muleContext);

    ZernovaObjectStoreManager osm = new ZernovaObjectStoreManager();
    osm.setBasePersistentStoreKey(BASE_PERSISTENT_OBJECT_STORE_KEY);
    osm.setBaseTransientStoreKey(BASE_IN_MEMORY_OBJECT_STORE_KEY);
    osm.setMuleContext(muleContext);
    registerObject(OBJECT_STORE_MANAGER, osm, muleContext);
  }

  protected void registerObjectStorePartitions(ZernovaContext muleContext) throws RegistrationException {
    registerObject(BASE_IN_MEMORY_OBJECT_STORE_KEY, getDefaultInMemoryObjectStore(), muleContext);
    registerObject(BASE_PERSISTENT_OBJECT_STORE_KEY, getDefaultPersistentObjectStore(), muleContext);
  }

  protected ObjectStore<Serializable> getDefaultPersistentObjectStore() {
    return createDefaultPersistentObjectStore();
  }

  protected ObjectStore<Serializable> getDefaultInMemoryObjectStore() {
    return createDefaultInMemoryObjectStore();
  }

  protected void configureQueueManager(ZernovaContext muleContext) throws RegistrationException {
    var queueManager = new TransactionalQueueManager();
    queueManager.setMuleConfiguration(muleContext.getConfiguration());
    queueManager.setMuleContext(muleContext);
    registerObject(OBJECT_QUEUE_MANAGER, queueManager, muleContext);
    registerObject(LOCAL_QUEUE_MANAGER_KEY, queueManager, muleContext);
  }

  private void configureComponentTracerFactory(ZernovaContext muleContext) throws RegistrationException {
    ComponentTracerFactory<CoreEvent> componentTracerFactory = getDummyComponentTracerFactory();
    registerObject(MULE_CORE_COMPONENT_TRACER_FACTORY_KEY, componentTracerFactory, muleContext);
  }

  protected void configureCoreTracer(ZernovaContext muleContext) throws RegistrationException {
    EventTracer<CoreEvent> tracer = getNoopCoreEventTracer();
    registerObject(MULE_CORE_EVENT_TRACER_KEY, tracer, muleContext);
  }

  protected void configureSpanExporterFactory(ZernovaContext muleContext) throws RegistrationException {
    SpanExporterFactory spanExporterFactory = new NoopSpanExporterFactory();
    registerObject(MULE_CORE_EXPORTER_FACTORY_KEY, spanExporterFactory, muleContext);
  }

  protected void configureSpanExporterConfiguration(ZernovaContext muleContext) throws RegistrationException {
    registerObject(MULE_SPAN_EXPORTER_CONFIGURATION_KEY, new EmptySpanExporterConfiguration(), muleContext);
  }

}
