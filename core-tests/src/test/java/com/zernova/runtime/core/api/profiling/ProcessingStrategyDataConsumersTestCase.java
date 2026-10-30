/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.profiling;

import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.FLOW;
import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.OPERATION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.FLOW_EXECUTED;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_OPERATION_EXECUTED;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_FLOW_MESSAGE_PASSING;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_SCHEDULING_FLOW_EXECUTION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_SCHEDULING_OPERATION_EXECUTION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.STARTING_FLOW_EXECUTION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_STARTING_OPERATION_EXECUTION;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_PROFILING_SERVICE_PROPERTY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.internal.profiling.consumer.ComponentProfilingUtils.LOCATION;
import static com.zernova.runtime.core.internal.profiling.consumer.ComponentProfilingUtils.getProcessingStrategyComponentInfoMap;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.DEFAULT_PROFILING_SERVICE;

import static java.util.Arrays.asList;

import static com.google.common.collect.ImmutableSet.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.ComponentProcessingStrategyProfilingEventContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;
import com.zernova.runtime.core.internal.profiling.NoOpProfilingService;
import com.zernova.runtime.core.internal.profiling.consumer.ComponentProcessingStrategyDataConsumer;
import com.zernova.runtime.core.internal.profiling.consumer.annotations.RuntimeInternalProfilingDataConsumer;
import com.zernova.runtime.core.internal.profiling.context.DefaultComponentProcessingStrategyProfilingEventContext;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;

import com.google.gson.Gson;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Feature(PROFILING)
@Story(DEFAULT_PROFILING_SERVICE)
@RunWith(Parameterized.class)
public class ProcessingStrategyDataConsumersTestCase extends AbstractZernovaContextTestCase {

  public static final String THREAD_NAME = "threadName";
  public static final String ARTIFACT_ID = "artifactId";
  public static final String ARTIFACT_TYPE = "artifactType";
  public static final long PROFILING_EVENT_TIMESTAMP = 5678L;
  public static final String CORRELATION_ID = "correlationId";

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Rule
  public SystemProperty enableProfilingServiceProperty = new SystemProperty(ENABLE_PROFILING_SERVICE_PROPERTY, "true");

  @Rule
  public EnableInternalRuntimeProfilers enableInternalRuntimeProfilers =
      new EnableInternalRuntimeProfilers(new TestComponentProcessingStrategyDataConsumer(new NoOpProfilingService(), null));

  @Mock
  private CoreEvent event;

  @Mock
  private ComponentLocation location;

  @Mock
  private Logger logger;

  @Mock
  private TypedComponentIdentifier componentIdentifier;

  @Mock
  private ComponentIdentifier identifier;

  private final Gson gson = new Gson();

  private final ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType;

  private ProfilingService profilingService;

  @Before
  public void before() throws Exception {
    when(logger.isDebugEnabled()).thenReturn(true);
    when(location.getComponentIdentifier()).thenReturn(componentIdentifier);
    when(location.getLocation()).thenReturn(LOCATION);
    when(componentIdentifier.getIdentifier()).thenReturn(identifier);
    when(identifier.getName()).thenReturn("test");
    when(identifier.getNamespace()).thenReturn("test");
    when(componentIdentifier.getType()).thenReturn(getTestComponentIdentifier());
    when(event.getCorrelationId()).thenReturn(CORRELATION_ID);
    profilingService = getTestProfilingService();
  }

  private TypedComponentIdentifier.ComponentType getTestComponentIdentifier() {
    if (profilingEventType == STARTING_FLOW_EXECUTION || profilingEventType == FLOW_EXECUTED) {
      return FLOW;
    } else {
      return OPERATION;
    }
  }

  private ProfilingService getTestProfilingService() throws ZernovaException {
    ProfilingService profilingService = new TestDefaultProfilingService(logger);
    initialiseIfNeeded(profilingService, muleContext);
    startIfNeeded(profilingService);
    return profilingService;
  }

  @Parameters(name = "eventType: {0}")
  public static Collection<ProfilingEventType<ComponentProcessingStrategyProfilingEventContext>> eventType() {
    return asList(PS_SCHEDULING_OPERATION_EXECUTION, PS_STARTING_OPERATION_EXECUTION, PS_OPERATION_EXECUTED,
                  PS_FLOW_MESSAGE_PASSING, PS_SCHEDULING_FLOW_EXECUTION, STARTING_FLOW_EXECUTION,
                  FLOW_EXECUTED);
  }

  public ProcessingStrategyDataConsumersTestCase(
                                                 ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType) {
    this.profilingEventType = profilingEventType;
  }

  @Test
  @Description("When a profiling event related to processing strategy is triggered, the data consumers process the data accordingly.")
  public void dataConsumersForProcessingStrategiesProfilingEventTypesConsumeDataAccordingly() {
    ProfilingDataProducer<ComponentProcessingStrategyProfilingEventContext, Object> dataProducer =
        profilingService.getProfilingDataProducer(profilingEventType);

    ComponentProcessingStrategyProfilingEventContext profilerEventContext =
        new DefaultComponentProcessingStrategyProfilingEventContext(event, location, THREAD_NAME, ARTIFACT_ID, ARTIFACT_TYPE,
                                                                    PROFILING_EVENT_TIMESTAMP);
    dataProducer.triggerProfilingEvent(profilerEventContext);
    verify(logger).debug(jsonToLog(profilingEventType, profilerEventContext));
  }

  /**
   * Stub {@link DefaultProfilingService} with a test {@link ProfilingDataConsumerDiscoveryStrategy}.
   */
  private static class TestDefaultProfilingService extends DefaultProfilingService {

    private final Logger logger;

    public TestDefaultProfilingService(Logger logger) {
      this.logger = logger;

    }

    @Override
    public ProfilingDataConsumerDiscoveryStrategy getDiscoveryStrategy() {
      return new TestProfilingDataConsumerDiscoveryStrategy(this, logger);
    }

  }


  /**
   * Stub {@link ProfilingDataConsumerDiscoveryStrategy}
   */
  private static class TestProfilingDataConsumerDiscoveryStrategy implements ProfilingDataConsumerDiscoveryStrategy {

    private final Logger logger;
    private final TestDefaultProfilingService profilingService;

    public TestProfilingDataConsumerDiscoveryStrategy(TestDefaultProfilingService profilingService, Logger logger) {
      this.profilingService = profilingService;
      this.logger = logger;
    }

    @Override
    public Set<ProfilingDataConsumer<?>> discover() {
      return of(new TestComponentProcessingStrategyDataConsumer(profilingService, logger));
    }
  }


  /**
   * Stub {@link ComponentProcessingStrategyDataConsumer} for injecting a mocked {@link Logger}
   */
  @RuntimeInternalProfilingDataConsumer
  private static class TestComponentProcessingStrategyDataConsumer extends ComponentProcessingStrategyDataConsumer {

    public TestComponentProcessingStrategyDataConsumer(InternalProfilingService profilingService, Logger logger) {
      super(profilingService, logger);
    }
  }

  private String jsonToLog(ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType,
                           ComponentProcessingStrategyProfilingEventContext profilingEventContext) {
    return gson.toJson(getProcessingStrategyComponentInfoMap(profilingEventType, profilingEventContext));
  }
}
