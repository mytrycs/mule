/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy.processor;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.api.notification.PipelineMessageNotification.PROCESS_COMPLETE;
import static com.zernova.runtime.api.notification.PipelineMessageNotification.PROCESS_END;
import static com.zernova.runtime.api.notification.PipelineMessageNotification.PROCESS_START;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.processor.strategy.AsyncProcessingStrategyFactory.DEFAULT_MAX_CONCURRENCY;
import static com.zernova.runtime.core.internal.processor.rector.profiling.ProfilingTestUtils.mockProcessingStrategyProfilingChainWithoutTriggeringEvent;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;
import static com.zernova.tck.util.ZernovaContextUtils.getNotificationDispatcher;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServicesWithProfilingService;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.deployment.management.ComponentInitialStateManager;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.notification.EnrichedServerNotification;
import com.zernova.runtime.api.notification.ErrorHandlerNotification;
import com.zernova.runtime.api.notification.FlowConstructNotification;
import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.notification.PipelineMessageNotification;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.management.stats.AllStatistics;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.construct.DefaultFlowBuilder.DefaultFlow;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ErrorHandler;
import com.zernova.runtime.core.internal.exception.ErrorHandlerFactory;
import com.zernova.runtime.core.internal.management.stats.DefaultFlowsSummaryStatistics;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;
import com.zernova.runtime.core.internal.transformer.ExtendedTransformationService;
import com.zernova.runtime.core.privileged.PrivilegedZernovaContext;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.processor.InternalProcessor;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChainBuilder;
import com.zernova.runtime.tracer.api.EventTracer;
import com.zernova.tck.junit4.AbstractReactiveProcessorTestCase;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.mockito.ArgumentMatcher;

@RunWith(Parameterized.class)
public class PipelineMessageNotificationTestCase extends AbstractReactiveProcessorTestCase {

  private CoreEvent event;
  private NotificationDispatcher notificationFirer;
  private TestPipeline pipeline;
  private final String pipelineName = "testPipeline";

  private EventContext context;

  public PipelineMessageNotificationTestCase(Mode mode) {
    super(mode);
  }

  @Before
  public void createMocks() throws Exception {
    muleContext.dispose();
    InternalProfilingService coreProfilingService = mock(InternalProfilingService.class);
    when(coreProfilingService.getCoreEventTracer()).thenReturn(mock(EventTracer.class));
    mockProcessingStrategyProfilingChainWithoutTriggeringEvent(coreProfilingService);
    muleContext = mockContextWithServicesWithProfilingService(coreProfilingService);
    when(muleContext.getStatistics()).thenReturn(new AllStatistics());
    when(muleContext.getConfiguration()).thenReturn(new DefaultZernovaConfiguration());
    notificationFirer = getNotificationDispatcher(muleContext);
    when(muleContext.getDefaultErrorHandler(empty())).thenReturn(new ErrorHandlerFactory().createDefault(notificationFirer));
    mockErrorTypeLocator();
    when(muleContext.getTransformationService()).thenReturn(new ExtendedTransformationService());
  }

  private void mockErrorTypeLocator() {
    ErrorTypeLocator typeLocator = mock(ErrorTypeLocator.class);
    ((ContributedErrorTypeLocator) ((PrivilegedZernovaContext) muleContext).getErrorTypeLocator()).setDelegate(typeLocator);

    ErrorType errorType = mock(ErrorType.class);
    when(errorType.getIdentifier()).thenReturn("ID");
    when(errorType.getNamespace()).thenReturn("NS");
    when(typeLocator.lookupErrorType(any(Throwable.class))).thenReturn(errorType);
    when(typeLocator.<String, Throwable>lookupComponentErrorType(any(ComponentIdentifier.class), any(Throwable.class)))
        .thenReturn(errorType);
  }

  public void createTestPipeline(List<Processor> processors, ErrorHandler errorHandler) {
    pipeline = new TestPipeline(pipelineName, muleContext, null, processors, errorHandler);
    pipeline.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    context = create(pipeline, TEST_CONNECTOR_LOCATION);
  }

  @After
  public void after() throws ZernovaException {
    stopIfNeeded(pipeline);
    disposeIfNeeded(pipeline, getLogger(getClass()));
    stopIfNeeded(muleContext.getSchedulerService());
  }

  @Test
  public void send() throws Exception {
    createTestPipeline(emptyList(), null);

    pipeline.initialise();
    pipeline.start();

    event = CoreEvent.builder(context).message(of("request")).build();

    process(pipeline, event);

    switch (mode) {
      case BLOCKING:
        new PollingProber().check(new JUnitLambdaProbe(() -> {
          verifySucess();
          return true;
        }));
        break;
      case NON_BLOCKING:
        new PollingProber().check(new JUnitLambdaProbe(() -> {
          verifySucess();
          return true;
        }));
        break;
      default:
        fail();
    }
  }

  @Test
  public void requestResponseException() throws Exception {
    createTestPipeline(singletonList(new ExceptionThrowingMessageProcessor()),
                       new ErrorHandlerFactory().createDefault(notificationFirer));

    pipeline.initialise();
    pipeline.start();

    event = CoreEvent.builder(context).message(of("request")).build();

    var thrown = assertThrows(MessagingException.class, () -> {
      try {
        process(pipeline, event);
      } finally {
        switch (mode) {
          case BLOCKING:
            new PollingProber().check(new JUnitLambdaProbe(() -> {
              verifyException();
              return true;
            }));
            break;
          case NON_BLOCKING:
            new PollingProber().check(new JUnitLambdaProbe(() -> {
              verifyException();
              return true;
            }));
            break;
          default:
            fail();
        }
      }
    });
    assertThat(thrown.getCause(), instanceOf(IllegalStateException.class));
  }

  private void verifySucess() {
    verify(notificationFirer, times(2)).dispatch(any(FlowConstructNotification.class));

    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(PROCESS_START, false, event)));
    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(PROCESS_END, false, event)));
    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(PROCESS_COMPLETE, false, event)));
    verify(notificationFirer, times(3)).dispatch(any(PipelineMessageNotification.class));
  }

  private void verifyException() {
    verify(notificationFirer, times(2)).dispatch(any(FlowConstructNotification.class));
    verify(notificationFirer, times(2)).dispatch(any(PipelineMessageNotification.class));
    verify(notificationFirer, times(2)).dispatch(any(ErrorHandlerNotification.class));

    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(PROCESS_START, false, event)));
    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(PROCESS_COMPLETE, true, event)));
    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(ErrorHandlerNotification.PROCESS_START, true, event)));
    verify(notificationFirer, times(1))
        .dispatch(argThat(new PipelineMessageNotificiationArgumentMatcher(ErrorHandlerNotification.PROCESS_END, true, event)));
  }

  private class TestPipeline extends DefaultFlow {

    CountDownLatch latch = new CountDownLatch(2);

    public TestPipeline(String name, ZernovaContext muleContext, MessageSource messageSource, List<Processor> messageProcessors,
                        ErrorHandler errorHandler) {
      super(name, muleContext, messageSource, messageProcessors, ofNullable(errorHandler), empty(), INITIAL_STATE_STARTED,
            DEFAULT_MAX_CONCURRENCY,
            new DefaultFlowsSummaryStatistics(true),
            new DefaultFlowsSummaryStatistics(true), createFlowStatistics(name, muleContext.getStatistics()),
            new ComponentInitialStateManager() {

              @Override
              public boolean mustStartMessageSource(Component component) {
                return true;
              }
            });
    }

    @Override
    protected void configureMessageProcessors(MessageProcessorChainBuilder builder) throws ZernovaException {
      builder.chain((Processor) event -> {
        latch.countDown();
        return event;
      });
      super.configureMessageProcessors(builder);
      builder.chain((Processor) event -> {
        latch.countDown();
        return event;
      });
    }

  }

  private class PipelineMessageNotificiationArgumentMatcher implements ArgumentMatcher<Notification> {

    private final int expectedAction;
    private final boolean exceptionExpected;
    private final CoreEvent event;

    public PipelineMessageNotificiationArgumentMatcher(int expectedAction, boolean exceptionExpected, CoreEvent event) {
      this.expectedAction = expectedAction;
      this.exceptionExpected = exceptionExpected;
      this.event = event;
    }

    @Override
    public boolean matches(Notification argument) {
      if (!(argument instanceof PipelineMessageNotification || argument instanceof ErrorHandlerNotification)) {
        return false;
      }

      EnrichedServerNotification notification = (EnrichedServerNotification) argument;
      Exception exception = notification.getException();

      boolean result = true;
      if (notification instanceof PipelineMessageNotification && exception != null) {
        result = ((MessagingException) exception).getFailingComponent()
            .equals(((PipelineMessageNotification) notification).getFailingComponent().get());
      }

      result = result &&
          expectedAction == notification.getAction().getActionId()
          && (notification.getEvent() == null) == (this.event == null)
          && (this.event == null ||
              (this.event.getMessage().getPayload().equals(notification.getEvent().getMessage().getPayload()) &&
                  this.event.getMessage().getAttributes().equals(notification.getEvent().getMessage().getAttributes())))
          && exceptionExpected == (exception != null);

      return result;
    }
  }

  public static class ExceptionThrowingMessageProcessor implements Processor, InternalProcessor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      throw new IllegalStateException();
    }
  }
}
