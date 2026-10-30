/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.management.stats;

import static com.zernova.runtime.core.api.construct.Flow.INITIAL_STATE_STARTED;
import static com.zernova.runtime.core.api.construct.Flow.INITIAL_STATE_STOPPED;
import static com.zernova.test.allure.AllureConstants.PricingMetricsFeature.FlowSummaryStory.ACTIVE_FLOWS_SUMMARY;
import static com.zernova.test.allure.AllureConstants.PricingMetricsFeature.PRICING_METRICS;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.management.stats.FlowsSummaryStatistics;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.construct.AbstractPipeline;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;

/**
 * Note: flow-mappings are not considered
 *
 */
@SmallTest
@Feature(PRICING_METRICS)
@Story(ACTIVE_FLOWS_SUMMARY)
public class FlowsSummaryStatisticsTestCase extends AbstractZernovaContextTestCase {

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();

    muleContext = spy(muleContext);
  }

  @Test
  public void triggerFlow() throws ZernovaException {
    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow = new TestPipeline("", muleContext,
                                         mock(MessageSource.class),
                                         INITIAL_STATE_STARTED,
                                         flowsSummaryStatistics);

    flow.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 1, 0, 0);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void apikitFlow() throws ZernovaException {
    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow = new TestPipeline("get:\\reservation:api-config", muleContext,
                                         null,
                                         INITIAL_STATE_STARTED,
                                         flowsSummaryStatistics);

    flow.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 1, 0, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 1, 0, 0, 1, 0);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 1, 0, 0, 0, 0);

    flow.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void apikitWithSourceFlow() throws ZernovaException {
    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow = new TestPipeline("get:\\reservation:api-config", muleContext,
                                         mock(MessageSource.class),
                                         INITIAL_STATE_STARTED,
                                         flowsSummaryStatistics);

    flow.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 1, 0, 0);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void privateFlow() throws ZernovaException {
    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow = new TestPipeline("", muleContext,
                                         null,
                                         INITIAL_STATE_STARTED,
                                         flowsSummaryStatistics);

    flow.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 1, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 1, 0, 0, 1);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 1, 0, 0, 0);

    flow.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void twoTriggerFlow() throws ZernovaException {
    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow1 = new TestPipeline("", muleContext,
                                          mock(MessageSource.class),
                                          INITIAL_STATE_STARTED,
                                          flowsSummaryStatistics);
    TestPipeline flow2 = new TestPipeline("", muleContext,
                                          mock(MessageSource.class),
                                          INITIAL_STATE_STARTED,
                                          flowsSummaryStatistics);

    flow1.initialise();
    flow2.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 2, 0, 0, 0, 0, 0);

    flow1.start();
    flow2.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 2, 0, 0, 2, 0, 0);

    flow1.stop();
    flow2.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 2, 0, 0, 0, 0, 0);

    flow1.dispose();
    flow2.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void triggerFlowRestarted() throws ZernovaException {
    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow = new TestPipeline("", muleContext,
                                         mock(MessageSource.class),
                                         INITIAL_STATE_STARTED,
                                         flowsSummaryStatistics);

    flow.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 1, 0, 0);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 1, 0, 0);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void triggerFlowInitialStateStopped() throws ZernovaException {
    doReturn(true).when(muleContext).isStarting();

    DefaultFlowsSummaryStatistics flowsSummaryStatistics = new DefaultFlowsSummaryStatistics(true);
    TestPipeline flow = new TestPipeline("", muleContext,
                                         mock(MessageSource.class),
                                         INITIAL_STATE_STOPPED,
                                         flowsSummaryStatistics);

    flow.initialise();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    doReturn(false).when(muleContext).isStarting();
    flow.start();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 1, 0, 0);

    flow.stop();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 1, 0, 0, 0, 0, 0);

    flow.dispose();
    assertFlowsSummaryStatistics(flowsSummaryStatistics, 0, 0, 0, 0, 0, 0);
  }

  private void assertFlowsSummaryStatistics(FlowsSummaryStatistics flowsSummaryStatistics,
                                            int expectedDeclaredTriggerFlows,
                                            int expectedDeclaredApikitFlows,
                                            int expectedDeclaredPrivateFlows,
                                            int expectedActiveTriggerFlows,
                                            int expectedActiveApikitFlows,
                                            int expectedActivePrivateFlows) {
    assertThat("declaredTriggerFlows",
               flowsSummaryStatistics.getDeclaredTriggerFlows(), is(expectedDeclaredTriggerFlows));
    assertThat("declaredApikitFlows",
               flowsSummaryStatistics.getDeclaredApikitFlows(), is(expectedDeclaredApikitFlows));
    assertThat("declaredPrivateFlows",
               flowsSummaryStatistics.getDeclaredPrivateFlows(), is(expectedDeclaredPrivateFlows));
    assertThat("activeTriggerFlows",
               flowsSummaryStatistics.getActiveTriggerFlows(), is(expectedActiveTriggerFlows));
    assertThat("activeApikitFlows",
               flowsSummaryStatistics.getActiveApikitFlows(), is(expectedActiveApikitFlows));
    assertThat("activePrivateFlows",
               flowsSummaryStatistics.getActivePrivateFlows(), is(expectedActivePrivateFlows));
  }

  private static class TestPipeline extends AbstractPipeline {

    public TestPipeline(String name, ZernovaContext muleContext, MessageSource source, String initialState,
                        DefaultFlowsSummaryStatistics flowsSummaryStatistics) {
      super(name, muleContext, source, emptyList(), empty(), empty(), initialState, null,
            flowsSummaryStatistics, new DefaultFlowsSummaryStatistics(true), null, null);
    }

    @Override
    public ComponentLocation getLocation() {
      return mock(ComponentLocation.class, RETURNS_DEEP_STUBS);
    }
  }

}
