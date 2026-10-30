/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENABLE_PROFILING_SERVICE;
import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.FORCE_RUNTIME_PROFILING_CONSUMERS_ENABLEMENT;
import static com.zernova.runtime.core.internal.profiling.notification.ProfilingNotification.getFullyQualifiedProfilingNotificationIdentifier;

import com.zernova.runtime.api.config.ZernovaRuntimeFeature;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingEventContext;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.FeatureFlaggingRegistry;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.internal.profiling.consumer.annotations.RuntimeInternalProfilingDataConsumer;
import com.zernova.runtime.core.internal.profiling.notification.ProfilingNotification;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;
import com.zernova.runtime.core.privileged.profiling.PrivilegedProfilingService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;

/**
 * A {@link AbstractProfilingService} that discovers available {@link ProfilingDataConsumer}
 *
 * @since 4.4
 */
public abstract class AbstractProfilingService
    implements InternalProfilingService, PrivilegedProfilingService, Initialisable, Startable, Stoppable {


  private static AtomicBoolean profilingFeaturesSet = new AtomicBoolean(false);

  @Inject
  protected ServerNotificationManager notificationManager;

  @Inject
  protected ProfilingFeatureFlaggingService featureFlaggingService;

  @Inject
  protected ZernovaContext muleContext;

  private final Set<NotificationListener<?>> addedListeners = new HashSet<>();

  @Override
  public void initialise() throws InitialisationException {}

  @Override
  public void start() throws ZernovaException {
    registerDataConsumers(getDiscoveryStrategy().discover());
  }

  private void registerDataConsumers(Set<ProfilingDataConsumer<?>> dataConsumers) {
    for (ProfilingDataConsumer<?> dataConsumer : dataConsumers) {
      doRegisterConsumer(dataConsumer);
    }
    registerNotificationListeners(dataConsumers);
    onDataConsumersRegistered();
  }

  private void doRegisterConsumer(ProfilingDataConsumer<?> dataConsumer) {
    Set<? extends ProfilingEventType<?>> profilingEventTypes = dataConsumer.getProfilingEventTypes();
    for (ProfilingEventType<?> profilingEventType : profilingEventTypes) {
      featureFlaggingService.registerProfilingFeature(profilingEventType, dataConsumer.getClass().getName());
      if (featureFlaggingService.isEnabled(FORCE_RUNTIME_PROFILING_CONSUMERS_ENABLEMENT)) {
        featureFlaggingService.toggleProfilingFeature(profilingEventType, dataConsumer.getClass().getName(),
                                                      isInternalDataConsumer(dataConsumer));
      }
    }
  }

  private boolean isInternalDataConsumer(ProfilingDataConsumer<?> dataConsumer) {
    return dataConsumer.getClass().isAnnotationPresent(RuntimeInternalProfilingDataConsumer.class);
  }

  /**
   * Invoked when new {@link ProfilingDataConsumer} is registered.
   */
  protected abstract void onDataConsumersRegistered();

  private void registerNotificationListeners(
                                             Set<ProfilingDataConsumer<?>> profilingDataConsumers) {
    profilingDataConsumers.forEach(this::registerNotificationListener);
  }

  private <T extends ProfilingEventContext> void registerNotificationListener(ProfilingDataConsumer<T> profilingDataConsumer) {
    NotificationListener<ProfilingNotification<T>> profilingNotificationListener =
        new DefaultProfilingNotificationListener<>(profilingDataConsumer);
    notificationManager
        .addListenerSubscription(profilingNotificationListener,
                                 pn -> filterByAction(profilingDataConsumer, pn));
    addedListeners.add(profilingNotificationListener);
  }

  private <T extends ProfilingEventContext> boolean filterByAction(ProfilingDataConsumer<T> profilingDataConsumer,
                                                                   ProfilingNotification<T> profilingNotification) {
    return profilingDataConsumer.getProfilingEventTypes().stream()
        .anyMatch(
                  eventType -> (getFullyQualifiedProfilingNotificationIdentifier(eventType))
                      .equalsIgnoreCase(profilingNotification.getActionName()))
        &&
        profilingDataConsumer.getEventContextFilter().test((T) profilingNotification.getSource());
  }

  @Override
  public void stop() {
    if (!notificationManager.isDisposed()) {
      addedListeners.forEach(listener -> notificationManager.removeListener(listener));
    }
  }

  /**
   * @return returns the {@link ProfilingDataConsumerDiscoveryStrategy} used by this service.
   */
  protected abstract ProfilingDataConsumerDiscoveryStrategy getDiscoveryStrategy();

  public <T extends ProfilingEventContext> void notifyEvent(T profilingEventContext, ProfilingEventType<T> action) {
    notificationManager.fireNotification(new ProfilingNotification<>(profilingEventContext, action));
  }

  /**
   * Configures the {@link ZernovaRuntimeFeature#ENABLE_PROFILING_SERVICE} feature flag.
   *
   * @since 4.4
   */
  public static void configureEnableProfilingService() {
    // TODO: Refactor Feature Flagging Service Initialization
    if (!profilingFeaturesSet.getAndSet(true)) {
      FeatureFlaggingRegistry featureFlaggingRegistry = FeatureFlaggingRegistry.getInstance();
      featureFlaggingRegistry.registerFeatureFlag(ENABLE_PROFILING_SERVICE,
                                                  featureContext -> false);
      featureFlaggingRegistry.registerFeatureFlag(FORCE_RUNTIME_PROFILING_CONSUMERS_ENABLEMENT,
                                                  featureContext -> false);
    }
  }

  @Override
  public <T extends ProfilingEventContext> void registerProfilingDataConsumer(ProfilingDataConsumer<T> profilingDataConsumer) {
    doRegisterConsumer(profilingDataConsumer);
    registerNotificationListener(profilingDataConsumer);
    onDataConsumersRegistered();
  }
}
