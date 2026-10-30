/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.util;

import static com.zernova.runtime.api.config.FeatureFlaggingService.FEATURE_FLAGGING_SERVICE_KEY;
import static com.zernova.runtime.config.internal.error.ZernovaCoreErrorTypeRepository.MULE_CORE_ERROR_TYPE_REPOSITORY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ARTIFACT_ENCODING;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONTEXT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_REGISTRY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STORE_MANAGER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.tck.junit4.AbstractZernovaTestCase.TEST_CONNECTOR_LOCATION;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static org.reflections.ReflectionUtils.Fields;
import static org.reflections.ReflectionUtils.Methods;
import static org.reflections.ReflectionUtils.get;
import static org.reflections.util.ReflectionUtilsPredicates.withAnnotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.deployment.management.ComponentInitialStateManager;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.management.stats.AllStatistics;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.util.UUID;
import com.zernova.runtime.core.internal.config.DefaultCustomizationService;
import com.zernova.runtime.core.internal.config.InternalCustomizationService;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeRepository;
import com.zernova.runtime.core.internal.exception.OnErrorPropagateHandler;
import com.zernova.runtime.core.internal.interception.InterceptorManager;
import com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistryHelper;
import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.runtime.core.privileged.PrivilegedZernovaContext;
import com.zernova.runtime.core.privileged.exception.DefaultExceptionListener;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.metrics.api.MeterProvider;
import com.zernova.runtime.metrics.api.error.ErrorMetricsFactory;
import com.zernova.runtime.tracer.api.EventTracer;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;
import com.zernova.tck.SimpleUnitTestSupportSchedulerService;
import com.zernova.tck.config.TestServicesConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import jakarta.inject.Inject;

/**
 * Provides helper methods to handle mock {@link ZernovaContext}s in unit tests.
 *
 * @since 4.0
 */
public class ZernovaContextUtils {

  private static final class MocksInjector implements Injector {

    private final Map<Class, Object> objects;

    private MocksInjector(Map<Class, Object> objects) {
      this.objects = objects;
    }

    @Override
    public <T> T inject(T object) {
      doInjectInto(object, Inject.class);
      // Still need to support javax.inject for the time being...
      doInjectInto(object, javax.inject.Inject.class);

      return object;
    }

    private <T> void doInjectInto(final T object, final Class<? extends Annotation> injectAnnClass) {
      for (Field field : get(Fields.of(object.getClass()), withAnnotation(injectAnnClass))) {
        Class<?> dependencyType = field.getType();

        boolean nullToOptional = false;
        if (dependencyType.equals(Optional.class)) {
          Type type = ((ParameterizedType) (field.getGenericType())).getActualTypeArguments()[0];
          if (type instanceof ParameterizedType) {
            dependencyType = (Class<?>) ((ParameterizedType) type).getRawType();
          } else {
            dependencyType = (Class<?>) type;
          }
          nullToOptional = true;
        }

        Object toInject = resolveObjectToInject(dependencyType);

        try {
          field.setAccessible(true);
          // Avoid overriding state already set by the test
          if (field.get(object) == null) {
            field.set(object, nullToOptional ? ofNullable(toInject) : toInject);
          }
        } catch (Exception e) {
          throw new RuntimeException(format("Could not inject dependency on field %s of type %s", field.getName(),
                                            object.getClass().getName()),
                                     e);
        }
      }
      for (Method method : get(Methods.of(object.getClass()), withAnnotation(injectAnnClass))) {
        if (method.getParameters().length == 1) {
          Class<?> dependencyType = method.getParameterTypes()[0];

          boolean nullToOptional = false;
          if (dependencyType.equals(Optional.class)) {
            Type type = ((ParameterizedType) (method.getGenericParameterTypes()[0])).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
              dependencyType = (Class<?>) ((ParameterizedType) type).getRawType();
            } else {
              dependencyType = (Class<?>) type;
            }
            nullToOptional = true;
          }

          Object toInject = resolveObjectToInject(dependencyType);

          try {
            method.setAccessible(true);
            method.invoke(object, nullToOptional ? of(toInject) : toInject);
          } catch (Exception e) {
            throw new RuntimeException(format("Could not inject dependency on method %s of type %s", method.getName(),
                                              object.getClass().getName()),
                                       e);
          }
        }

      }
    }

    private Object resolveObjectToInject(Class<?> dependencyType) {
      if (objects.containsKey(dependencyType)) {
        return objects.get(dependencyType);
      } else if (Collection.class.isAssignableFrom(dependencyType)) {
        return emptySet();
      } else if (Object.class.equals(dependencyType)) {
        return null;
      } else {
        return mock(dependencyType);
      }
    }
  }

  private ZernovaContextUtils() {
    // No instances of this class allowed
  }

  public static ZernovaContext mockMuleContext() {
    ZernovaRegistryHelper registry = mock(ZernovaRegistryHelper.class, withSettings().lenient());

    ComponentInitialStateManager componentInitialStateManager =
        mock(ComponentInitialStateManager.class, withSettings().lenient());
    when(componentInitialStateManager.mustStartMessageSource(any())).thenReturn(true);
    when(registry.getDelegate()).thenReturn(registry);
    when(registry.lookupObject(ComponentInitialStateManager.SERVICE_ID)).thenReturn(componentInitialStateManager);
    when(registry.lookupObject(FEATURE_FLAGGING_SERVICE_KEY)).thenReturn(mock(FeatureFlaggingService.class));
    when(registry.lookupObject(OBJECT_ARTIFACT_ENCODING)).thenReturn(mock(ArtifactEncoding.class));

    final ZernovaContextWithRegistry muleContext = (ZernovaContextWithRegistry) mockMuleContext(registry);

    when(registry.lookupObject(OBJECT_REGISTRY)).thenReturn(new DefaultRegistry(muleContext));
    when(registry.lookupObject(OBJECT_MULE_CONTEXT)).thenReturn(muleContext);

    return muleContext;
  }

  public static ZernovaContext mockMuleContext(ZernovaRegistry registry) {
    final ZernovaContextWithRegistry muleContext =
        mock(DefaultZernovaContext.class,
             withSettings().defaultAnswer(RETURNS_DEEP_STUBS).extraInterfaces(PrivilegedZernovaContext.class).lenient());
    when(muleContext.getUniqueIdString()).thenReturn(UUID.getUUID());
    OnErrorPropagateHandler onError = new OnErrorPropagateHandler();
    onError.setExceptionListener(new DefaultExceptionListener());
    when(muleContext.getDefaultErrorHandler(empty())).thenReturn(onError);

    AllStatistics allStatistics = new AllStatistics();
    when(muleContext.getStatistics()).thenReturn(allStatistics);

    StreamingManager streamingManager = mock(StreamingManager.class, RETURNS_DEEP_STUBS);
    try {
      when(muleContext.getRegistry()).thenReturn(registry);
      doReturn(streamingManager).when(registry).lookupObject(StreamingManager.class);
      doReturn(mock(NotificationDispatcher.class)).when(registry).lookupObject(NotificationDispatcher.class);
      doReturn(mock(InterceptorManager.class)).when(registry).lookupObject(InterceptorManager.class);
      doReturn(mock(ObjectStoreManager.class, RETURNS_DEEP_STUBS)).when(registry).lookupObject(OBJECT_STORE_MANAGER);
    } catch (RegistrationException e) {
      throw new RuntimeException(e);
    }

    when(muleContext.getExecutionClassLoader()).thenReturn(ZernovaContextUtils.class.getClassLoader());

    return muleContext;
  }

  public static NotificationDispatcher getNotificationDispatcher(ZernovaContext muleContext) throws RegistrationException {
    return ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(NotificationDispatcher.class);
  }

  /**
   * Creates and configures a mock {@link ZernovaContext} to return testing services implementations.
   *
   * @param coreProfilingService profiling service to use.
   *
   * @return the created {@code muleContext}.
   */
  public static ZernovaContext mockContextWithServicesWithProfilingService(InternalProfilingService coreProfilingService) {
    final ZernovaContextWithRegistry muleContext = (ZernovaContextWithRegistry) mockMuleContext();
    final ZernovaRegistry registry = muleContext.getRegistry();
    doMockContextWithServicesWithProfilingService(coreProfilingService, registry, muleContext);
    return muleContext;
  }

  /**
   * Creates and configures a mock {@link ZernovaContext} to return testing services implementations.
   *
   * @param coreProfilingService profiling service to use.
   *
   * @return the created {@code muleContext}.
   */
  public static ZernovaContext mockContextWithServicesWithProfilingService(InternalProfilingService coreProfilingService,
                                                                        ZernovaRegistry registry) {
    final ZernovaContextWithRegistry muleContext = (ZernovaContextWithRegistry) mockMuleContext(registry);
    doMockContextWithServicesWithProfilingService(coreProfilingService, registry, muleContext);
    return muleContext;
  }

  private static void doMockContextWithServicesWithProfilingService(InternalProfilingService coreProfilingService,
                                                                    ZernovaRegistry registry,
                                                                    final ZernovaContextWithRegistry muleContext) {
    final ExtensionManager extensionManager = mock(ExtensionManager.class, withSettings().lenient());
    when(extensionManager.getExtensions()).thenReturn(emptySet());
    when(muleContext.getExtensionManager()).thenReturn(extensionManager);

    InternalCustomizationService customServices = new DefaultCustomizationService();
    new TestServicesConfigurationBuilder().configure(customServices);
    when(muleContext.getCustomizationService()).thenReturn(customServices);

    SchedulerService schedulerService = spy(new SimpleUnitTestSupportSchedulerService());
    when(muleContext.getSchedulerService()).thenReturn(schedulerService);

    ContributedErrorTypeRepository errorTypeRepository = new ContributedErrorTypeRepository();
    errorTypeRepository.setDelegate(MULE_CORE_ERROR_TYPE_REPOSITORY);
    when(muleContext.getErrorTypeRepository()).thenReturn(errorTypeRepository);

    ErrorTypeLocator typeLocator = new ContributedErrorTypeLocator();
    when(((PrivilegedZernovaContext) muleContext).getErrorTypeLocator()).thenReturn(typeLocator);

    final ZernovaConfiguration configuration = muleContext.getConfiguration();
    lenient().when(configuration.getMinMuleVersion()).thenReturn(of(new ZernovaVersion(getMuleManifest().getProductVersion())));

    NotificationListenerRegistry notificationListenerRegistry = mock(NotificationListenerRegistry.class);
    ConfigurationProperties configProps = mock(ConfigurationProperties.class, withSettings().lenient());
    when(configProps.resolveBooleanProperty(any())).thenReturn(empty());

    FeatureFlaggingService featureFlaggingService = mock(FeatureFlaggingService.class);

    ConfigurationComponentLocator configurationComponentLocator =
        mock(ConfigurationComponentLocator.class, withSettings().lenient());
    when(configurationComponentLocator.find(any(Location.class))).thenReturn(empty());
    when(configurationComponentLocator.find(any(ComponentIdentifier.class))).thenReturn(emptyList());
    when(muleContext.getArtifactType()).thenReturn(APP);

    try {
      when(registry.lookupObject(NotificationListenerRegistry.class)).thenReturn(notificationListenerRegistry);
      when(registry.lookupObject(ComponentTracerFactory.class)).thenReturn(new DummyComponentTracerFactory());

      Map<Class, Object> injectableObjects = new HashMap<>();
      injectableObjects.put(ZernovaContext.class, muleContext);
      injectableObjects.put(SchedulerService.class, schedulerService);
      injectableObjects.put(ErrorTypeRepository.class, errorTypeRepository);
      injectableObjects.put(ErrorTypeLocator.class, typeLocator);
      injectableObjects.put(ExtendedExpressionManager.class, muleContext.getExpressionManager());
      injectableObjects.put(StreamingManager.class, muleContext.getRegistry().lookupObject(StreamingManager.class));
      injectableObjects.put(ObjectStoreManager.class, muleContext.getRegistry().lookupObject(OBJECT_STORE_MANAGER));
      injectableObjects.put(NotificationDispatcher.class,
                            muleContext.getRegistry().lookupObject(NotificationDispatcher.class));
      injectableObjects.put(NotificationListenerRegistry.class, notificationListenerRegistry);
      injectableObjects.put(ConfigurationComponentLocator.class, configurationComponentLocator);
      injectableObjects.put(ConfigurationProperties.class, configProps);
      injectableObjects.put(FeatureFlaggingService.class, featureFlaggingService);
      injectableObjects.put(InternalProfilingService.class, coreProfilingService);
      injectableObjects.put(ProfilingService.class, coreProfilingService);
      injectableObjects.put(ComponentTracerFactory.class, new DummyComponentTracerFactory());
      injectableObjects.put(ErrorMetricsFactory.class, ErrorMetricsFactory.NO_OP);
      injectableObjects.put(MeterProvider.class, MeterProvider.NO_OP);

      // Ensure injection of consistent mock objects
      when(muleContext.getInjector()).thenReturn(new MocksInjector(injectableObjects));
    } catch (RegistrationException e1) {
      throw new ZernovaRuntimeException(e1);
    }
  }

  /**
   * Creates and configures a mock {@link ZernovaContext} to return testing services implementations.
   *
   * @return the created {@code muleContext}.
   */
  public static ZernovaContext mockContextWithServices() {
    InternalProfilingService profilingService = mock(InternalProfilingService.class);
    EventTracer<CoreEvent> muleCoreEventTracer = mock(EventTracer.class);
    when(profilingService.getProfilingDataProducer(any(ProfilingEventType.class))).thenReturn(mock(ProfilingDataProducer.class));
    when(profilingService.getCoreEventTracer()).thenReturn(muleCoreEventTracer);
    return mockContextWithServicesWithProfilingService(profilingService);
  }

  /**
   * Creates and configures a mock {@link ZernovaContext} to return testing services implementations.
   *
   * @return the created {@code muleContext}.
   */
  public static ZernovaContext mockContextWithServices(ZernovaRegistry registry) {
    InternalProfilingService profilingService = mock(InternalProfilingService.class);
    EventTracer<CoreEvent> muleCoreEventTracer = mock(EventTracer.class);
    when(profilingService.getProfilingDataProducer(any(ProfilingEventType.class))).thenReturn(mock(ProfilingDataProducer.class));
    when(profilingService.getCoreEventTracer()).thenReturn(muleCoreEventTracer);
    return mockContextWithServicesWithProfilingService(profilingService, registry);
  }

  /**
   * Adds an extension model to a mocked {@link ZernovaContext}.
   *
   * @param muleContext    the mocked {@link ZernovaContext}.
   * @param extensionModel the {@link ExtensionModel} to be added.
   */
  public static void addExtensionModelToMock(ZernovaContext muleContext, ExtensionModel extensionModel) {
    ExtensionManager extensionManager = muleContext.getExtensionManager();
    Set<ExtensionModel> extensionModels = new LinkedHashSet<>(extensionManager.getExtensions());
    extensionModels.add(extensionModel);
    when(extensionManager.getExtensions()).thenReturn(extensionModels);
  }

  /**
   * Adds a mock registry to the provided {@code context}. If the context is not a mock, it is {@link Mockito#spy(Class)}ied.
   */
  public static void mockRegistry(ZernovaContext context) {
    ((ZernovaContextWithRegistry) doReturn(spy(((ZernovaContextWithRegistry) context).getRegistry())).when(context)).getRegistry();
  }

  /**
   * Configures the registry in the provided {@code context} to return the given {@code value} for the given {@code key}.
   */
  public static void registerIntoMockContext(ZernovaContext context, String key, Object value) {
    when(((ZernovaContextWithRegistry) context).getRegistry().lookupObject(key)).thenReturn(value);
    when(((ZernovaContextWithRegistry) context).getRegistry().get(key)).thenReturn(value);
  }

  /**
   * Configures the registry in the provided {@code context} to return the given {@code value} for the given {@code clazz}.
   */
  public static <T> void registerIntoMockContext(ZernovaContext context, Class<T> clazz, T value) {
    try {
      when(((ZernovaContextWithRegistry) context).getRegistry().lookupObject(clazz)).thenReturn(value);
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  /**
   * Creates a basic event builder with its context already set.
   */
  public static <B extends CoreEvent.Builder> B eventBuilder() throws ZernovaException {
    return eventBuilder(mockContextWithServices());
  }

  /**
   * Creates a basic event builder with its context built from the provided {@code muleContext}.
   */
  public static <B extends CoreEvent.Builder> B eventBuilder(ZernovaContext muleContext) throws ZernovaException {
    return (B) CoreEvent
        .builder(create(muleContext.getUniqueIdString(), muleContext.getId(), TEST_CONNECTOR_LOCATION, null, empty()));
  }

  public static void verifyRegistration(ZernovaContext muleContext, String registryKey, ArgumentCaptor captor) {
    ZernovaRegistry registry = ((ZernovaContextWithRegistry) muleContext).getRegistry();
    try {
      verify(registry).registerObject(eq(registryKey), captor.capture());
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  public static void verifyRegistration(ZernovaContext muleContext, Class valueClass) {
    ZernovaRegistry registry = ((ZernovaContextWithRegistry) muleContext).getRegistry();
    try {
      verify(registry).registerObject(anyString(), any(valueClass));
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  public static void verifyExactRegistration(ZernovaContext muleContext, String key, Object value) {
    ZernovaRegistry registry = ((ZernovaContextWithRegistry) muleContext).getRegistry();
    try {
      verify(registry).registerObject(eq(key), eq(value));
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  public static void whenRegistration(ZernovaContext muleContext, Answer answer) {
    ZernovaRegistry registry = ((ZernovaContextWithRegistry) muleContext).getRegistry();
    try {
      doAnswer(answer).when(registry).registerObject(anyString(), any());
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  public static <R> R getRegistry(ZernovaContext muleContext, Class<R> registryClass) {
    ArgumentCaptor<Registry> registryCaptor = ArgumentCaptor.forClass(Registry.class);
    ((ZernovaContextWithRegistry) verify(muleContext, atLeastOnce())).setRegistry(registryCaptor.capture());
    List<Registry> registries = registryCaptor.getAllValues();

    assertThat(registries.get(0), instanceOf(registryClass));

    return (R) registries.get(0);
  }
}

