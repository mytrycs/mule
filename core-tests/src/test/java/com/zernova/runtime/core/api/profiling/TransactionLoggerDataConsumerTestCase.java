/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.profiling;


import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_COMMIT;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_CONTINUE;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_ROLLBACK;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_START;
import static com.zernova.runtime.api.tx.TransactionType.LOCAL;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_PROFILING_SERVICE_PROPERTY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.internal.profiling.consumer.ComponentProfilingUtils.getTxInfo;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.DEFAULT_PROFILING_SERVICE;

import static java.util.Arrays.asList;
import static com.google.common.collect.ImmutableSet.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.TransactionProfilingEventContext;
import com.zernova.runtime.core.internal.profiling.DefaultProfilingService;
import com.zernova.runtime.core.internal.profiling.consumer.TransactionLoggerDataConsumer;
import com.zernova.runtime.core.internal.profiling.consumer.annotations.RuntimeInternalProfilingDataConsumer;
import com.zernova.runtime.core.internal.profiling.context.DefaultTransactionProfilingEventContext;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.util.Collection;
import java.util.Optional;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

@Feature(PROFILING)
@Story(DEFAULT_PROFILING_SERVICE)
@RunWith(Parameterized.class)
public class TransactionLoggerDataConsumerTestCase extends AbstractZernovaContextTestCase {

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Rule
  public SystemProperty enableProfilingServiceProperty = new SystemProperty(ENABLE_PROFILING_SERVICE_PROPERTY, "true");

  @Rule
  public EnableInternalRuntimeProfilers enableInternalRuntimeProfilers =
      new EnableInternalRuntimeProfilers(new TestTransactionLoggerDataConsumer(null));

  @Mock
  private Logger logger;

  @Mock
  private ComponentLocation originalLocation;

  @Mock
  private ComponentLocation currentLocation;

  private final Gson gson = new Gson();

  private final ProfilingEventType<TransactionProfilingEventContext> profilingEventType;

  private ProfilingService profilingService;

  public TransactionLoggerDataConsumerTestCase(ProfilingEventType<TransactionProfilingEventContext> profilingEventType) {
    this.profilingEventType = profilingEventType;
  }

  @Parameterized.Parameters(name = "eventType: {0}")
  public static Collection<ProfilingEventType<TransactionProfilingEventContext>> eventType() {
    return asList(TX_COMMIT, TX_CONTINUE, TX_ROLLBACK, TX_START);
  }

  @Before
  public void before() throws Exception {
    when(logger.isDebugEnabled()).thenReturn(true);
    when(originalLocation.getLocation()).thenReturn("someflow/2");
    when(currentLocation.getLocation()).thenReturn("someflow/2/error-handler/0");
    profilingService = getTestProfilingService();
  }

  private ProfilingService getTestProfilingService() throws ZernovaException {
    ProfilingService profilingService = new TestDefaultProfilingService(logger);
    initialiseIfNeeded(profilingService, muleContext);
    startIfNeeded(profilingService);
    return profilingService;
  }

  @Test
  @Description("When a profiling event related to transactions is triggered, the data consumers process the data accordingly.")
  public void dataConsumersForProcessingStrategiesProfilingEventTypesConsumeDataAccordingly() {
    ProfilingDataProducer<TransactionProfilingEventContext, Object> dataProducer =
        profilingService.getProfilingDataProducer(profilingEventType);

    TransactionProfilingEventContext profilerEventContext =
        new DefaultTransactionProfilingEventContext(Optional.of(originalLocation), currentLocation, LOCAL, 0);
    dataProducer.triggerProfilingEvent(profilerEventContext);

    verify(logger).debug(jsonToLog(profilingEventType, profilerEventContext));
  }

  private static class TestDefaultProfilingService extends DefaultProfilingService {

    private final Logger logger;

    public TestDefaultProfilingService(Logger logger) {
      this.logger = logger;

    }

    @Override
    public ProfilingDataConsumerDiscoveryStrategy getDiscoveryStrategy() {
      return new TestTransactionDataConsumerDiscoveryStrategy(logger);
    }

  }

  /**
   * Stub {@link ProfilingDataConsumerDiscoveryStrategy}
   */
  private static class TestTransactionDataConsumerDiscoveryStrategy implements ProfilingDataConsumerDiscoveryStrategy {

    private final Logger logger;

    public TestTransactionDataConsumerDiscoveryStrategy(Logger logger) {
      this.logger = logger;
    }

    @Override
    public Set<ProfilingDataConsumer<?>> discover() {
      return of(new TestTransactionLoggerDataConsumer(logger));
    }
  }

  /**
   * Stub {@link TransactionLoggerDataConsumer} for injecting a mocked {@link Logger}
   */
  @RuntimeInternalProfilingDataConsumer
  private static class TestTransactionLoggerDataConsumer extends TransactionLoggerDataConsumer {

    private final Logger logger;

    public TestTransactionLoggerDataConsumer(Logger logger) {
      super();
      this.logger = logger;
    }

    @Override
    protected Logger getDataConsumerLogger() {
      return logger;
    }
  }

  private String jsonToLog(ProfilingEventType<TransactionProfilingEventContext> profilingEventType,
                           TransactionProfilingEventContext profilingEventContext) {
    return gson.toJson(getTxInfo(profilingEventType, profilingEventContext));
  }
}
