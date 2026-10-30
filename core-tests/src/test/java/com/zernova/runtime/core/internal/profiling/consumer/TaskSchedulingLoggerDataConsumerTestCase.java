/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.consumer;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.SCHEDULING_TASK_EXECUTION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.STARTING_TASK_EXECUTION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TASK_EXECUTED;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_PROFILING_SERVICE_PROPERTY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.profiling.ProcessingStrategyDataConsumersTestCase.ARTIFACT_ID;
import static com.zernova.runtime.core.api.profiling.ProcessingStrategyDataConsumersTestCase.ARTIFACT_TYPE;
import static com.zernova.runtime.core.api.profiling.ProcessingStrategyDataConsumersTestCase.PROFILING_EVENT_TIMESTAMP;
import static com.zernova.runtime.core.api.profiling.ProcessingStrategyDataConsumersTestCase.THREAD_NAME;
import static com.zernova.runtime.core.internal.processor.rector.profiling.ProfilingTestUtils.enableProfilingFeatureTestConsumer;
import static com.zernova.runtime.core.internal.profiling.consumer.ComponentProfilingUtils.getTaskSchedulingInfoMap;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.DEFAULT_PROFILING_SERVICE;

import org.junit.After;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.TaskSchedulingProfilingEventContext;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.profiling.tracing.DefaultComponentMetadata;
import com.zernova.runtime.core.internal.profiling.tracing.DefaultExecutionContext;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.Collection;
import java.util.Collections;

import com.zernova.tck.junit4.rule.SystemProperty;
import org.slf4j.Logger;
import com.google.gson.Gson;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Feature(PROFILING)
@Story(DEFAULT_PROFILING_SERVICE)
@RunWith(Parameterized.class)
public class TaskSchedulingLoggerDataConsumerTestCase extends AbstractZernovaContextTestCase {

  private static final String TASK_ID = "taskId";
  private static final String CORRELATION_ID = "correlationId";

  private final Gson gson = new Gson();
  private ProfilingService profilingService;
  private final ProfilingEventType<TaskSchedulingProfilingEventContext> profilingEventType;

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Rule
  public SystemProperty systemProperty = new SystemProperty(ENABLE_PROFILING_SERVICE_PROPERTY, "true");

  @Mock
  private TaskSchedulingProfilingEventContext profilingEventContext;
  @Mock
  private Logger logger;
  @Mock
  private TypedComponentIdentifier componentIdentifier;
  @Mock
  private ComponentIdentifier identifier;
  @Mock
  private ComponentLocation location;

  public TaskSchedulingLoggerDataConsumerTestCase(ProfilingEventType<TaskSchedulingProfilingEventContext> profilingEventType) {
    this.profilingEventType = profilingEventType;
  }

  @Parameterized.Parameters(name = "eventType: {0}")
  public static Collection<ProfilingEventType<TaskSchedulingProfilingEventContext>> eventTypes() {
    return asList(SCHEDULING_TASK_EXECUTION, STARTING_TASK_EXECUTION, TASK_EXECUTED);
  }

  @Before
  public void before() throws Exception {
    when(logger.isDebugEnabled()).thenReturn(true);
    when(location.getComponentIdentifier()).thenReturn(componentIdentifier);
    when(componentIdentifier.getIdentifier()).thenReturn(identifier);
    when(identifier.getName()).thenReturn("test");
    when(identifier.getNamespace()).thenReturn("test");
    profilingService = getTestProfilingService();
    when(profilingEventContext.getTriggerTimestamp()).thenReturn(PROFILING_EVENT_TIMESTAMP);
    when(profilingEventContext.getThreadName()).thenReturn((THREAD_NAME));
    when(profilingEventContext.getTaskId()).thenReturn(TASK_ID);
    when(profilingEventContext.getTaskTracingContext()).thenReturn(of(new DefaultExecutionContext(
                                                                                                  new DefaultComponentMetadata(CORRELATION_ID,
                                                                                                                               ARTIFACT_ID,
                                                                                                                               ARTIFACT_TYPE,
                                                                                                                               location))));
    setProfilingFeatureStatus(true);
  }

  @After
  public void after() throws Exception {
    setProfilingFeatureStatus(false);
  }

  private void setProfilingFeatureStatus(boolean status) {
    eventTypes().forEach(eventType -> {
      try {
        enableProfilingFeatureTestConsumer(muleContext, eventType, status);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private ProfilingService getTestProfilingService() throws ZernovaException {
    ProfilingService profilingService = new DefaultProfilingService() {

      @Override
      public ProfilingDataConsumerDiscoveryStrategy getDiscoveryStrategy() {
        return () -> Collections.singleton(new TaskSchedulingLoggerDataConsumer() {

          @Override
          protected Logger getDataConsumerLogger() {
            return logger;
          }
        });
      }
    };
    initialiseIfNeeded(profilingService, muleContext);
    startIfNeeded(profilingService);
    return profilingService;
  }

  @Test
  @Description("When a profiling event related to task scheduling is triggered, the data consumer logs the data accordingly.")
  public void dataConsumerLogs() {
    ProfilingDataProducer<TaskSchedulingProfilingEventContext, ?> dataProducer =
        profilingService.getProfilingDataProducer(profilingEventType);
    dataProducer.triggerProfilingEvent(profilingEventContext);
    verify(logger).debug(jsonToLog(profilingEventType, profilingEventContext));
  }

  @Test
  @Description("When a profiling event related to task scheduling is triggered, the data consumer filter events with empty execution context.")
  public void dataConsumerFilter() {
    when(profilingEventContext.getTaskTracingContext()).thenReturn(null);
    ProfilingDataProducer<TaskSchedulingProfilingEventContext, ?> dataProducer =
        profilingService.getProfilingDataProducer(profilingEventType);
    dataProducer.triggerProfilingEvent(profilingEventContext);
    verifyNoInteractions(logger);
  }

  private String jsonToLog(ProfilingEventType<TaskSchedulingProfilingEventContext> profilingEventType,
                           TaskSchedulingProfilingEventContext profilingEventContext) {
    return gson.toJson(getTaskSchedulingInfoMap(profilingEventType, profilingEventContext));
  }

}
