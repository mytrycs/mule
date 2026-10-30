/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.source.polling;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_POLICY_MANAGER;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.tck.ZernovaTestUtils.APPLE_FLOW;
import static com.zernova.tck.ZernovaTestUtils.createFlowWithSource;
import static com.zernova.test.allure.AllureConstants.SchedulerFeature.SCHEDULER;
import static com.zernova.test.allure.AllureConstants.SchedulerFeature.SchedulerStories.SCHEDULED_FLOW_EXECUTION;

import static java.util.Collections.singletonMap;
import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.source.scheduler.FixedFrequencyScheduler;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.execution.ZernovaMessageProcessingManager;
import com.zernova.runtime.core.internal.policy.PolicyManager;
import com.zernova.runtime.core.internal.policy.SourcePolicy;
import com.zernova.runtime.core.internal.source.scheduler.DefaultSchedulerMessageSource;
import com.zernova.tck.SensingNullMessageProcessor;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.probe.PollingProber;
import com.zernova.tck.probe.Probe;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.qameta.allure.Issue;
import org.slf4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SCHEDULER)
@Story(SCHEDULED_FLOW_EXECUTION)
public class DefaultSchedulerMessageSourceTestCase extends AbstractZernovaContextTestCase {

  private static final Logger LOGGER = getLogger(DefaultSchedulerMessageSourceTestCase.class);
  private Flow flow;
  private PolicyManager policyManager;
  private SourcePolicy sourcePolicy;
  private final String MESSAGE_PROCESSING_MANAGER_KEY = "_muleMessageProcessingManager";

  @Before
  public void setUp() throws Exception {
    policyManager = mock(PolicyManager.class);
    sourcePolicy = mock(SourcePolicy.class);
    when(policyManager.createSourcePolicyInstance(any(), any(), any(), any())).thenReturn(sourcePolicy);

    ZernovaMessageProcessingManager processingManager = new ZernovaMessageProcessingManager();
    processingManager.setMuleContext(muleContext);

    ((DefaultZernovaContext) muleContext).getRegistry().unregisterObject(OBJECT_POLICY_MANAGER);
    ((DefaultZernovaContext) muleContext).getRegistry().registerObject(OBJECT_POLICY_MANAGER, policyManager);
    ((DefaultZernovaContext) muleContext).getRegistry().unregisterObject(MESSAGE_PROCESSING_MANAGER_KEY);
    ((DefaultZernovaContext) muleContext).getRegistry().registerObject(MESSAGE_PROCESSING_MANAGER_KEY, processingManager);
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(REGISTRY_KEY, componentLocator);
  }

  @Test
  public void simplePoll() throws Exception {

    DefaultSchedulerMessageSource schedulerMessageSource = createMessageSource();

    SensingNullMessageProcessor flow = getSensingNullMessageProcessor();
    schedulerMessageSource.setListener(flow);
    schedulerMessageSource.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));

    doAnswer(invocationOnMock -> {
      CoreEvent inputEvent = invocationOnMock.getArgument(0);
      flow.process(inputEvent);
      return null;
    }).when(sourcePolicy).process(any(CoreEvent.class), any(), any());

    schedulerMessageSource.trigger();
    new PollingProber(RECEIVE_TIMEOUT, 100).check(new Probe() {

      @Override
      public boolean isSatisfied() {
        return flow.event != null;
      }

      @Override
      public String describeFailure() {
        return "flow event never set by the source flow";
      }
    });
  }

  @Test
  @Issue("W-17981248")
  public void testStartWhenStopping() throws Exception {
    DefaultSchedulerMessageSource schedulerMessageSource = createMessageSource();
    SensingNullMessageProcessor flow = getSensingNullMessageProcessor();
    schedulerMessageSource.setListener(flow);
    schedulerMessageSource.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));

    DefaultZernovaContext spyMuleContext = spy((DefaultZernovaContext) muleContext);
    doReturn(true).when(spyMuleContext).isStopping();

    schedulerMessageSource.setMuleContext(spyMuleContext);
    schedulerMessageSource.start();

    assertThat("Scheduler should not be started when ZernovaContext is stopping",
               schedulerMessageSource.isStarted(), is(false));
  }

  @Test
  @Issue("W-17981248")
  public void testRunWhenStopping() throws Exception {
    DefaultSchedulerMessageSource schedulerMessageSource = createMessageSource();
    SensingNullMessageProcessor flow = getSensingNullMessageProcessor();
    schedulerMessageSource.setListener(flow);
    schedulerMessageSource.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));

    DefaultZernovaContext spyMuleContext = spy((DefaultZernovaContext) muleContext);
    doReturn(false).when(spyMuleContext).isStopping();
    doReturn(true).when(spyMuleContext).isPrimaryPollingInstance();

    schedulerMessageSource.setMuleContext(spyMuleContext);
    schedulerMessageSource.start();
    doReturn(true).when(spyMuleContext).isStopping();

    verify(spyMuleContext, atLeastOnce()).isStopping();
    // isPrimaryPollingInstance() was never called if isStopping() returns true
    verify(spyMuleContext, never()).isPrimaryPollingInstance();
    assertThat("Flow should not be executed when ZernovaContext is stopping", flow.event, is(nullValue()));
  }

  @Test
  @Issue("W-17981248")
  public void testPollWhenStopping() throws Exception {
    DefaultSchedulerMessageSource schedulerMessageSource = createMessageSource();
    SensingNullMessageProcessor flow = getSensingNullMessageProcessor();
    schedulerMessageSource.setListener(flow);
    schedulerMessageSource.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));

    DefaultZernovaContext spyMuleContext = spy((DefaultZernovaContext) muleContext);
    doReturn(true).when(spyMuleContext).isStopping();

    schedulerMessageSource.setMuleContext(spyMuleContext);
    schedulerMessageSource.trigger();
    Thread.sleep(100);

    verify(spyMuleContext, times(1)).isStopping();
    assertThat("Flow should not be executed when ZernovaContext is stopping", flow.event, is(nullValue()));
  }

  @Test
  public void disposeScheduler() throws Exception {
    SchedulerService schedulerService = muleContext.getSchedulerService();
    reset(schedulerService);

    AtomicReference<Scheduler> pollScheduler = new AtomicReference<>();

    doAnswer(invocation -> {
      if (pollScheduler.get() == null) {
        Scheduler scheduler = (Scheduler) invocation.callRealMethod();
        final Scheduler spiedScheduler = spy(scheduler);
        doAnswer(inv -> {
          scheduler.stop();
          return null;
        }).when(spiedScheduler).stop();
        pollScheduler.set(spiedScheduler);
      }
      return pollScheduler.get();
    }).when(schedulerService).cpuLightScheduler(any(SchedulerConfig.class));

    DefaultSchedulerMessageSource schedulerMessageSource = createMessageSource();
    final ArgumentCaptor<SchedulerConfig> schedulerConfigCaptor = forClass(SchedulerConfig.class);
    verify(schedulerService, atLeastOnce()).cpuLightScheduler(schedulerConfigCaptor.capture());
    assertThat(schedulerConfigCaptor.getValue().getSchedulerName(),
               is("DefaultMessageProcessorChain '(chain) of 'appleFlow' processor chain' .switchOnErrorScheduler"));

    schedulerMessageSource.start();

    verify(pollScheduler.get()).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());

    schedulerMessageSource.stop();
    schedulerMessageSource.dispose();

    verify(pollScheduler.get()).stop();
  }

  private DefaultSchedulerMessageSource schedulerMessageSource;

  @After
  public void after() throws ZernovaException {
    stopIfNeeded(schedulerMessageSource);
    disposeIfNeeded(schedulerMessageSource, LOGGER);

    stopIfNeeded(flow);
    disposeIfNeeded(flow, LOGGER);
  }

  private DefaultSchedulerMessageSource createMessageSource() throws Exception {
    schedulerMessageSource = new DefaultSchedulerMessageSource(scheduler(), false);
    schedulerMessageSource.setAnnotations(getAppleFlowComponentLocationAnnotations());

    // Manually create and register flow
    flow = createFlowWithSource(muleContext, APPLE_FLOW, schedulerMessageSource);
    when(componentLocator.find(Location.builder().globalName(APPLE_FLOW).build())).thenReturn(of(flow));
    // scheduler source is initialized when it's registered as the flow's source in the registry
    ((DefaultZernovaContext) muleContext).getRegistry().registerFlowConstruct(flow);

    startIfNeeded(flow);

    // Injecting processing manager dependency
    muleContext.getInjector().inject(schedulerMessageSource);
    return schedulerMessageSource;
  }

  private FixedFrequencyScheduler scheduler() {
    FixedFrequencyScheduler factory = new FixedFrequencyScheduler();
    factory.setFrequency(1000);
    return factory;
  }

}
