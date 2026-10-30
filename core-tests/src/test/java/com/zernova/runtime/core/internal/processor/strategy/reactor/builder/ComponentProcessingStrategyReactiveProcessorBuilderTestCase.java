/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy.reactor.builder;

import static java.util.Arrays.asList;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_OPERATION_EXECUTED;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_FLOW_MESSAGE_PASSING;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_SCHEDULING_OPERATION_EXECUTION;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.PS_STARTING_OPERATION_EXECUTION;
import static com.zernova.runtime.core.internal.processor.rector.profiling.ProfilingTestUtils.mockProcessingStrategyProfilingChain;
import static com.zernova.runtime.core.internal.processor.strategy.reactor.builder.ComponentProcessingStrategyReactiveProcessorBuilder.processingStrategyReactiveProcessorFrom;
import static com.zernova.test.allure.AllureConstants.ProcessingStrategiesFeature.PROCESSING_STRATEGIES;
import static com.zernova.test.allure.AllureConstants.ProcessingStrategiesFeature.ProcessingStrategiesStory.REACTOR;

import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.internal.processor.strategy.enricher.AbstractEnrichedReactiveProcessorTestCase;
import com.zernova.runtime.core.internal.util.rx.ImmediateScheduler;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;

import java.util.Collection;
import java.util.concurrent.Callable;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Feature(PROCESSING_STRATEGIES)
@Story(REACTOR)
@RunWith(Parameterized.class)
public class ComponentProcessingStrategyReactiveProcessorBuilderTestCase extends AbstractEnrichedReactiveProcessorTestCase {

  private final int parallelism;

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Spy
  private ImmediateScheduler callbackScheduler;

  @Spy
  private ImmediateScheduler dispatcherScheduler;

  @Mock
  private Scheduler contextScheduler;

  @Mock
  private CoreEvent coreEvent;

  @Mock
  private InternalProfilingService profilingService;

  @Mock
  private ProfilingDataProducer profilingDataProducer;

  public ComponentProcessingStrategyReactiveProcessorBuilderTestCase(int parallelism) {
    this.parallelism = parallelism;
  }

  @Parameters(name = "parallelism: {0}")
  public static Collection<Integer> paralellism() {
    return asList(1, 4);
  }

  @Before
  public void before() {
    mockProcessingStrategyProfilingChain(profilingService, profilingDataProducer);
  }

  @Test
  @Description("The reactor chain created by the builder uses the set schedulers and emits the core events")
  public void pipelineReactiveProcessorBuilt() {
    ReactiveProcessor transform =
        processingStrategyReactiveProcessorFrom(reactiveProcessor, contextScheduler, ARTIFACT_ID, ARTIFACT_TYPE)
            .withDispatcherScheduler(dispatcherScheduler)
            .withCallbackScheduler(callbackScheduler)
            .withProfilingService(profilingService)
            .withParallelism(parallelism)
            .build();

    createAndExecuteEnrichedTransformer(transform, coreEvent);

    verify(callbackScheduler, atLeastOnce()).submit(any(Callable.class));
    verify(dispatcherScheduler, atLeastOnce()).submit(any(Callable.class));

    // The profiling data producers are obtained
    verify(profilingService, atLeastOnce()).getProfilingDataProducer(PS_FLOW_MESSAGE_PASSING);
    verify(profilingService, atLeastOnce()).getProfilingDataProducer(PS_OPERATION_EXECUTED);
    verify(profilingService, atLeastOnce()).getProfilingDataProducer(PS_STARTING_OPERATION_EXECUTION);
    verify(profilingService, atLeastOnce()).getProfilingDataProducer(PS_SCHEDULING_OPERATION_EXECUTION);
    verify(profilingDataProducer, atLeastOnce()).triggerProfilingEvent(any());
  }
}
