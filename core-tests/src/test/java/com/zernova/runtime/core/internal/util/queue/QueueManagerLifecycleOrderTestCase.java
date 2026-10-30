/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.queue;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.config.FeatureFlaggingService.FEATURE_FLAGGING_SERVICE_KEY;
import static com.zernova.runtime.ast.api.error.ErrorTypeRepositoryProvider.CORE_ERROR_TYPE_REPO;
import static com.zernova.runtime.core.api.config.ZernovaProperties.INTERCEPTOR_MANAGER_REGISTRY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_ERROR_METRICS_FACTORY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_METER_PROVIDER_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_PROFILING_SERVICE_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ALERTING_SUPPORT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_NOTIFICATION_DISPATCHER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_QUEUE_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SECURITY_MANAGER;
import static com.zernova.runtime.core.api.processor.strategy.AsyncProcessingStrategyFactory.DEFAULT_MAX_CONCURRENCY;
import static com.zernova.runtime.core.internal.processor.rector.profiling.ProfilingTestUtils.mockProcessingStrategyProfilingChainWithoutTriggeringEvent;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.deployment.management.ComponentInitialStateManager;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.alert.ZernovaAlertingSupport;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.builders.SimpleConfigurationBuilder;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.api.util.queue.QueueConfiguration;
import com.zernova.runtime.core.api.util.queue.QueueManager;
import com.zernova.runtime.core.api.util.queue.QueueSession;
import com.zernova.runtime.core.internal.construct.DefaultFlowBuilder;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.interception.InterceptorManager;
import com.zernova.runtime.core.internal.management.stats.DefaultFlowsSummaryStatistics;
import com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;
import com.zernova.runtime.core.internal.security.DefaultZernovaSecurityManager;
import com.zernova.runtime.metrics.api.MeterProvider;
import com.zernova.runtime.metrics.api.error.ErrorMetricsFactory;
import com.zernova.runtime.tracer.customization.api.InitialSpanInfoProvider;
import com.zernova.tck.config.TestServicesConfigurationBuilder;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@SmallTest
public class QueueManagerLifecycleOrderTestCase extends AbstractZernovaContextTestCase {

  private final List<Object> startStopOrder = new ArrayList<>();
  private final RecordingTQM rtqm = new RecordingTQM();

  @Rule
  public TestServicesConfigurationBuilder testServicesConfigurationBuilder = new TestServicesConfigurationBuilder();

  private ZernovaContext muleContext;

  @Before
  public void before() throws InitialisationException, ConfigurationException {
    Map<String, Object> objects = new HashMap<>();
    objects.put(FEATURE_FLAGGING_SERVICE_KEY, mock(FeatureFlaggingService.class));
    objects.put(OBJECT_QUEUE_MANAGER, rtqm);
    objects.put(OBJECT_SECURITY_MANAGER, new DefaultZernovaSecurityManager());
    objects.put(INTERCEPTOR_MANAGER_REGISTRY_KEY, mock(InterceptorManager.class));
    InternalProfilingService coreProfilingService = mock(InternalProfilingService.class);
    objects.put(MULE_PROFILING_SERVICE_KEY, coreProfilingService);
    objects.put(MULE_ERROR_METRICS_FACTORY_KEY, ErrorMetricsFactory.NO_OP);
    objects.put(MULE_METER_PROVIDER_KEY, MeterProvider.NO_OP);

    mockProcessingStrategyProfilingChainWithoutTriggeringEvent(coreProfilingService);

    muleContext = new DefaultZernovaContextFactory().createMuleContext(testServicesConfigurationBuilder,
                                                                    new SimpleConfigurationBuilder(objects));
    testServicesConfigurationBuilder.configure(muleContext);
  }

  @After
  public void after() {
    muleContext.dispose();
  }

  @Test
  public void testStartupOrder() throws Exception {
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(OBJECT_ALERTING_SUPPORT,
                                                                         mock(ZernovaAlertingSupport.class));
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(OBJECT_NOTIFICATION_DISPATCHER,
                                                                         mock(NotificationDispatcher.class));
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(OBJECT_NOTIFICATION_DISPATCHER,
                                                                         mock(NotificationDispatcher.class));
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(ErrorTypeRepository.class.getName(),
                                                                         CORE_ERROR_TYPE_REPO.get());
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject(InitialSpanInfoProvider.class.getName(),
                                                                         new DummyComponentTracerFactory());
    FlowConstruct fc = new RecordingFlow("dummy", muleContext);
    fc.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerFlowConstruct(fc);
    muleContext.start();
    muleContext.stop();
    assertEquals(4, startStopOrder.size());
    assertSame(rtqm, startStopOrder.get(0));
    assertSame(fc, startStopOrder.get(1));
    assertSame(fc, startStopOrder.get(2));
    assertSame(rtqm, startStopOrder.get(3));

  }

  private class RecordingTQM implements QueueManager {

    @Override
    public void start() throws ZernovaException {
      startStopOrder.add(this);
    }

    @Override
    public void stop() throws ZernovaException {
      startStopOrder.add(this);
    }

    @Override
    public QueueSession getQueueSession() {
      throw notImplementedException();
    }

    @Override
    public void setDefaultQueueConfiguration(QueueConfiguration config) {
      throw notImplementedException();
    }

    @Override
    public void setQueueConfiguration(String queueName, QueueConfiguration config) {
      throw notImplementedException();
    }

    @Override
    public Optional<QueueConfiguration> getQueueConfiguration(String queueName) {
      throw notImplementedException();
    }

    private NotImplementedException notImplementedException() {
      return new NotImplementedException("This is test code");
    }
  }

  private class RecordingFlow extends DefaultFlowBuilder.DefaultFlow {

    public RecordingFlow(String name, ZernovaContext muleContext) {
      super(name, muleContext, null, emptyList(), empty(), empty(), INITIAL_STATE_STARTED, DEFAULT_MAX_CONCURRENCY,
            new DefaultFlowsSummaryStatistics(true),
            new DefaultFlowsSummaryStatistics(true), null,
            new ComponentInitialStateManager() {

              @Override
              public boolean mustStartMessageSource(Component component) {
                return true;
              }
            });
    }

    @Override
    public void doStart() throws ZernovaException {
      super.doStart();
      startStopOrder.add(this);
    }

    @Override
    public void doStop() throws ZernovaException {
      startStopOrder.add(this);
      super.doStop();
    }
  }
}
