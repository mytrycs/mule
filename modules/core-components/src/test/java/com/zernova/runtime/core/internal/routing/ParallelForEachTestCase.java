/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.api.metadata.DataType.MULE_MESSAGE_LIST;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_PRINT_DETAILED_COMPOSITE_EXCEPTION_LOG_PROPERTY;
import static com.zernova.runtime.core.internal.streaming.CursorUtils.unwrap;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;
import static com.zernova.tck.ZernovaTestUtils.APPLE_FLOW;
import static com.zernova.tck.processor.ContextPropagationChecker.assertContextPropagation;
import static com.zernova.tck.util.ZernovaContextUtils.eventBuilder;
import static com.zernova.test.allure.AllureConstants.ScopeFeature.SCOPE;
import static com.zernova.test.allure.AllureConstants.ScopeFeature.ParallelForEachStory.PARALLEL_FOR_EACH;
import static reactor.core.publisher.Flux.from;

import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.expression.ExpressionRuntimeException;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.internal.routing.ForkJoinStrategy.RoutingPair;
import com.zernova.runtime.core.internal.routing.forkjoin.CollectListForkJoinStrategyFactory;
import com.zernova.runtime.core.internal.streaming.ManagedCursorProvider;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.tck.SensingNullMessageProcessor;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.tck.processor.ContextPropagationChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.DescriptorKey;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@Feature(SCOPE)
@Story(PARALLEL_FOR_EACH)
public class ParallelForEachTestCase extends AbstractZernovaContextTestCase {

  @Rule
  public SystemProperty detailedCompositeRoutingExceptionLog;

  @Rule
  public ExpectedException expectedException = none();

  private final ParallelForEach router = new ParallelForEach();
  private final ForkJoinStrategyFactory mockForkJoinStrategyFactory = mock(ForkJoinStrategyFactory.class);

  public ParallelForEachTestCase(boolean detailedCompositeRoutingExceptionLog) {
    this.detailedCompositeRoutingExceptionLog = new SystemProperty(MULE_PRINT_DETAILED_COMPOSITE_EXCEPTION_LOG_PROPERTY,
                                                                   Boolean.toString(detailedCompositeRoutingExceptionLog));
  }

  @Parameterized.Parameters(name = "Detailed log: {0}")
  public static List<Object[]> parameters() {
    return asList(
                  new Object[] {true},
                  new Object[] {false});
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    when(componentLocator.find(Location.builder().globalName(APPLE_FLOW).build())).thenReturn(of(mock(Flow.class)));
    return singletonMap(REGISTRY_KEY, componentLocator);
  }

  @After
  public void tearDown() {
    router.dispose();
  }

  @Test
  @Description("RoutingPairs are created for each route configured. Each RoutingPair has the same input event.")
  public void routingPairs() throws Exception {
    CoreEvent event = createListEvent();

    MessageProcessorChain nested = mock(MessageProcessorChain.class);
    muleContext.getInjector().inject(router);
    router.setMessageProcessors(singletonList(nested));
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    List<RoutingPair> routingPairs = from(router.getRoutingPairs(event)).collectList().block();
    assertThat(routingPairs, hasSize(2));
    assertThat(routingPairs.get(0).getEvent().getMessage().getPayload().getValue(),
               equalTo(((List<Message>) event.getMessage().getPayload().getValue()).get(0)));
    assertThat(routingPairs.get(1).getEvent().getMessage().getPayload().getValue(),
               equalTo(((List<Message>) event.getMessage().getPayload().getValue()).get(1)));
  }

  @Test
  @Description("By default the router result populates the outgoing message payload.")
  public void defaultTarget() throws Exception {
    CoreEvent original = createListEvent();

    MessageProcessorChain nested = newChain(empty(), event -> event);
    nested.setMuleContext(muleContext);
    router.setMessageProcessors(singletonList(nested));

    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    Event result = router.process(original);

    assertThat(result.getMessage().getPayload().getValue(), instanceOf(List.class));
    List<Message> resultList = (List<Message>) result.getMessage().getPayload().getValue();
    assertThat(resultList, hasSize(2));
  }

  @Test
  @Description("When a custom target is configured the router result is set in a variable and the input event is output.")
  public void customTargetMessage() throws Exception {
    final String variableName = "foo";

    CoreEvent original = createListEvent();

    MessageProcessorChain nested = newChain(empty(), event -> event);
    nested.setMuleContext(muleContext);
    router.setMessageProcessors(singletonList(nested));

    router.setTarget(variableName);
    router.setTargetValue("#[message]");
    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    Event result = router.process(original);

    assertThat(result.getMessage(), equalTo(original.getMessage()));
    assertThat(((Message) result.getVariables().get(variableName).getValue()).getPayload().getValue(), instanceOf(List.class));
    List<Message> resultList =
        (List<Message>) ((Message) result.getVariables().get(variableName).getValue()).getPayload().getValue();
    assertThat(resultList, hasSize(2));
  }

  @Test
  @Description("When a custom target is configured the router result is set in a variable and the input event is output.")
  public void customTargetDefaultPayload() throws Exception {
    final String variableName = "foo";

    CoreEvent original = createListEvent();

    MessageProcessorChain nested = newChain(empty(), event -> event);
    nested.setMuleContext(muleContext);
    router.setMessageProcessors(singletonList(nested));
    router.setTarget(variableName);
    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    Event result = router.process(original);

    assertThat(result.getMessage(), equalTo(original.getMessage()));
    final TypedValue<?> typedValue = result.getVariables().get(variableName);
    assertThat(typedValue.getValue(), instanceOf(List.class));
    assertThat(List.class.isAssignableFrom(typedValue.getDataType().getType()), is(true));
    List<Message> resultList = (List<Message>) typedValue.getValue();
    assertThat(resultList, hasSize(2));
  }

  @Test
  @Description("Cursor provider should be managed by cursor manager inside parallel-foreach.")
  @Issue("MULE-18573")
  public void cursorStreamShouldBeManagedByCursorManager() throws Exception {
    CursorProvider cursorProvider = mock(CursorStreamProvider.class);
    CoreEvent original = getEventBuilder().message(Message.of(singletonList(cursorProvider))).build();

    MessageProcessorChain nested = newChain(empty(), event -> event);
    nested.setMuleContext(muleContext);
    router.setMessageProcessors(singletonList(nested));

    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    Event result = router.process(original);

    assertThat(result.getMessage().getPayload().getValue(), instanceOf(List.class));
    List<Message> resultList = (List<Message>) result.getMessage().getPayload().getValue();
    assertThat(resultList, hasSize(1));

    assertThat(resultList.get(0).getPayload().getValue(), is(instanceOf(ManagedCursorProvider.class)));
    ManagedCursorProvider managedCursorProvider = (ManagedCursorProvider) resultList.get(0).getPayload().getValue();
    assertThat(unwrap(managedCursorProvider), is(sameInstance(cursorProvider)));
  }

  @Test
  @Description("The router uses a fork-join strategy with concurrency and timeout configured via the router and delayErrors true.")
  public void forkJoinStrategyConfiguration() throws Exception {
    final int concurrency = 3;
    final long timeout = 123;

    router.setMaxConcurrency(concurrency);
    router.setTimeout(timeout);
    router.setMessageProcessors(singletonList(mock(MessageProcessorChain.class)));
    router.setForkJoinStrategyFactory(mockForkJoinStrategyFactory);

    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    verify(mockForkJoinStrategyFactory).createForkJoinStrategy(any(ProcessingStrategy.class), eq(concurrency), eq(true),
                                                               eq(timeout),
                                                               any(Scheduler.class), any(ErrorType.class),
                                                               any(Scheduler.class),
                                                               eq(Boolean.parseBoolean(detailedCompositeRoutingExceptionLog
                                                                   .getValue())));
  }

  @Test
  @Description("By default CollectListForkJoinStrategyFactory is used which aggregates routes into a message with a List<Message> payload.")
  public void defaultForkJoinStrategyFactory() {
    assertThat(router.getDefaultForkJoinStrategyFactory(), instanceOf(CollectListForkJoinStrategyFactory.class));
    assertThat(router.getDefaultForkJoinStrategyFactory().getResultDataType(), equalTo(MULE_MESSAGE_LIST));
  }

  @Test
  @Description("Delay errors is always true for scatter-gather currently.")
  public void defaultDelayErrors() {
    assertThat(router.isDelayErrors(), equalTo(true));
  }

  @Test
  @DescriptorKey("An invalid collection expression result in a ExpressionRuntimeException")
  public void failingExpression() throws Exception {
    SensingNullMessageProcessor nullMessageProcessor = new SensingNullMessageProcessor();
    router.setMessageProcessors(singletonList(nullMessageProcessor));
    router.setCollectionExpression("!@INVALID");

    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    expectedException.expect(MessagingException.class);
    expectedException.expectCause(instanceOf(ExpressionRuntimeException.class));
    router.process(testEvent());
  }

  @Test
  public void subscriberContextPropagation() throws ZernovaException {
    final ContextPropagationChecker contextPropagationChecker = new ContextPropagationChecker();

    router.setMessageProcessors(singletonList(contextPropagationChecker));
    muleContext.getInjector().inject(router);
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    assertContextPropagation(eventBuilder(muleContext).message(Message.of(asList("1", "2", "3"))).build(), router,
                             contextPropagationChecker);
  }

  private CoreEvent createListEvent() throws ZernovaException {
    List<String> arrayList = new ArrayList<>();
    arrayList.add("bar");
    arrayList.add("zip");
    return getEventBuilder().message(Message.of(arrayList)).build();
  }
}
