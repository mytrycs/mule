/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.construct;

import static com.zernova.runtime.core.internal.construct.FlowBackPressureException.createFlowBackPressureException;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.deployment.management.ComponentInitialStateManager;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.BackPressureReason;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.api.management.stats.FlowConstructStatistics;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.management.stats.DefaultFlowsSummaryStatistics;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;

@SmallTest
public class AbstractPipelineTestCase extends AbstractZernovaContextTestCase {

  private static class TestPipeline extends AbstractPipeline {

    public TestPipeline(String name, ZernovaContext muleContext, MessageSource source, List<Processor> processors,
                        Optional<FlowExceptionHandler> exceptionListener,
                        Optional<ProcessingStrategyFactory> processingStrategyFactory, String initialState,
                        Integer maxConcurrency,
                        DefaultFlowsSummaryStatistics flowsSummaryStatistics, FlowConstructStatistics flowConstructStatistics,
                        ComponentInitialStateManager componentInitialStateManager) {
      super(name, muleContext, source, processors, exceptionListener, processingStrategyFactory, initialState, maxConcurrency,
            new DefaultFlowsSummaryStatistics(true), // TODO W-18668900: remove once the pilot is concluded
            flowsSummaryStatistics, flowConstructStatistics, componentInitialStateManager);
    }

    @Override
    public ComponentLocation getLocation() {
      return mock(ComponentLocation.class, RETURNS_DEEP_STUBS);
    }
  }

  private AbstractPipeline abstractPipeline;

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();

    MessageSource messageSource = mock(MessageSource.class);
    DefaultFlowsSummaryStatistics flowSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    FlowConstructStatistics flowConstructStatistics = mock(FlowConstructStatistics.class);
    ComponentInitialStateManager componentInitialStateManager = mock(ComponentInitialStateManager.class);
    abstractPipeline = new TestPipeline(
                                        "TestPipeline",
                                        muleContext,
                                        messageSource,
                                        emptyList(),
                                        empty(),
                                        empty(),
                                        "test initial state",
                                        1,
                                        flowSummaryStatistics,
                                        flowConstructStatistics,
                                        componentInitialStateManager);
    abstractPipeline.initialise();
  }

  @After
  public void after() throws ZernovaException {
    if (abstractPipeline.getLifecycleState().isInitialised()) {
      abstractPipeline.dispose();
    }
  }

  @Test
  @Issue("MULE-20032")
  @Description("Checks that the pre-built back pressure exceptions have the same type and reason as defined by FlowBackPressureException")
  public void testBackpressureExceptionsHaveCorrectTypeAndReason() {
    for (BackPressureReason backPressureReason : BackPressureReason.values()) {
      // We expect that cached exceptions in the abstract pipeline would be created with the same internal mapping defined
      // by FlowBackPressureException
      FlowBackPressureException expectedFlowBackPressureException =
          createFlowBackPressureException(abstractPipeline, backPressureReason);
      FlowBackPressureException actualFlowBackPressureException =
          abstractPipeline.getBackPressureExceptions().get(backPressureReason);

      assertThat(actualFlowBackPressureException, instanceOf(expectedFlowBackPressureException.getClass()));
      assertThat(actualFlowBackPressureException.getMessage(), is(equalTo(expectedFlowBackPressureException.getMessage())));
    }
  }
}
