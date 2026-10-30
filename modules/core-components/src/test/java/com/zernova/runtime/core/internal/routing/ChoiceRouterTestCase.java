/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static com.zernova.functional.junit4.matchers.ThrowableMessageMatcher.hasMessage;
import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.management.stats.RouterStatistics.TYPE_OUTBOUND;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;
import static com.zernova.tck.processor.ContextPropagationChecker.assertContextPropagation;
import static com.zernova.tck.util.ZernovaContextUtils.eventBuilder;
import static com.zernova.test.allure.AllureConstants.ScopeFeature.SCOPE;
import static com.zernova.test.allure.AllureConstants.ScopeFeature.ChoiceStory.CHOICE;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.el.ExpressionExecutionException;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.expression.ExpressionRuntimeException;
import com.zernova.runtime.core.api.management.stats.RouterStatistics;
import com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.tck.junit4.AbstractReactiveProcessorTestCase;
import com.zernova.tck.processor.ContextPropagationChecker;
import com.zernova.tck.testmodels.mule.TestMessageProcessor;

import java.util.Map;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(SCOPE)
@Story(CHOICE)
public class ChoiceRouterTestCase extends AbstractReactiveProcessorTestCase {

  @Rule
  public ExpectedException thrown = none();

  private ChoiceRouter choiceRouter;

  public ChoiceRouterTestCase(Mode mode) {
    super(mode);
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    choiceRouter = new ChoiceRouter(new DummyComponentTracerFactory());
    choiceRouter.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));
    choiceRouter.setExpressionManager(muleContext.getExpressionManager());
  }

  @After
  public void after() {
    disposeIfNeeded(choiceRouter, getLogger(getClass()));
  }

  @Test
  public void noRoute() throws Exception {
    initialise();

    CoreEvent inputEvent = fooEvent();
    assertThat(process(choiceRouter, inputEvent), is(inputEvent));
  }

  @Test
  public void onlyDefaultRoute() throws Exception {
    choiceRouter.setDefaultRoute(newChain(empty(), new TestMessageProcessor("default")));
    initialise();

    assertThat(process(choiceRouter, fooEvent()).getMessage().getPayload().getValue(), is("foo:default"));
  }

  @Test
  public void noMatchingNorDefaultRoute() throws Exception {
    choiceRouter.addRoute(payloadZapExpression(), newChain(empty(), new TestMessageProcessor("bar")));
    initialise();

    CoreEvent inputEvent = fooEvent();
    assertThat(process(choiceRouter, inputEvent), is(inputEvent));
  }

  @Test
  public void noMatchingRouteWithDefaultRoute() throws Exception {
    choiceRouter.addRoute(payloadZapExpression(), newChain(empty(), new TestMessageProcessor("bar")));
    choiceRouter.setDefaultRoute(newChain(empty(), new TestMessageProcessor("default")));
    initialise();

    assertThat(process(choiceRouter, fooEvent()).getMessage().getPayload().getValue(), is("foo:default"));
  }

  @Test
  public void matchingRouteWithDefaultRoute() throws Exception {
    choiceRouter.addRoute(payloadZapExpression(), newChain(empty(), new TestMessageProcessor("bar")));
    choiceRouter.setDefaultRoute(newChain(empty(), new TestMessageProcessor("default")));
    initialise();

    assertThat(process(choiceRouter, zapEvent()).getMessage().getPayload().getValue(), is("zap:bar"));
  }

  @Test
  public void matchingRouteWithStatistics() throws Exception {
    TestMessageProcessor processor = new TestMessageProcessor("bar");
    choiceRouter.addRoute(payloadZapExpression(), newChain(empty(), processor));
    RouterStatistics routerStatistics = new RouterStatistics(TYPE_OUTBOUND);
    routerStatistics.setEnabled(true);
    choiceRouter.setRouterStatistics(routerStatistics);
    initialise();

    assertThat(process(choiceRouter, zapEvent()).getMessage().getPayload().getValue(), is("zap:bar"));
    assertThat(process(choiceRouter, zapEvent()).getMessage().getPayload().getValue(), is("zap:bar"));
    assertThat((Map<String, Long>) routerStatistics.getRouted(), hasEntry(containsString(processor.toString()), is((long) 2)));
  }

  @Test
  @Issue("MULE-19512")
  public void failingExpression() throws Exception {
    MessageProcessorChain mp = newChain(empty(), new TestMessageProcessor("bar"));
    choiceRouter.addRoute("wat", mp);
    initialise();

    thrown.expect(instanceOf(ExpressionRuntimeException.class));
    thrown.expect(hasMessage(containsString("evaluating expression: \"wat\"")));
    thrown.expectCause(instanceOf(ExpressionExecutionException.class));
    process(choiceRouter, zapEvent());
  }

  @Test
  public void failingRoute() throws Exception {
    final DefaultZernovaException expected = new DefaultZernovaException("Oops");

    MessageProcessorChain mp = newChain(empty(), event -> {
      throw expected;
    });
    choiceRouter.addRoute(payloadZapExpression(), mp);
    initialise();

    thrown.expect(sameInstance(expected));
    process(choiceRouter, zapEvent());
  }

  @Test
  public void failingRouteDefault() throws Exception {
    final DefaultZernovaException expected = new DefaultZernovaException("Oops");

    MessageProcessorChain mp = newChain(empty(), event -> {
      throw expected;
    });
    choiceRouter.addRoute(payloadZapExpression(), mp);
    choiceRouter.setDefaultRoute(newChain(empty(), new TestMessageProcessor("bar")));
    initialise();

    thrown.expect(sameInstance(expected));
    process(choiceRouter, zapEvent());
  }

  @Test
  public void subscriberContextPropagation() throws ZernovaException {
    final ContextPropagationChecker contextPropagationChecker = new ContextPropagationChecker();

    MessageProcessorChain mp = newChain(empty(), contextPropagationChecker);
    choiceRouter.addRoute(payloadZapExpression(), mp);
    initialise();

    assertContextPropagation(zapEvent(), choiceRouter, contextPropagationChecker);
  }

  @Test
  public void subscriberContextPropagationDefaultRoute() throws ZernovaException {
    final ContextPropagationChecker contextPropagationChecker = new ContextPropagationChecker();

    MessageProcessorChain mp = newChain(empty(), contextPropagationChecker);
    choiceRouter.addRoute(payloadZapExpression(), event -> event);
    choiceRouter.setDefaultRoute(mp);
    initialise();

    assertContextPropagation(fooEvent(), choiceRouter, contextPropagationChecker);
  }

  private void initialise() throws InitialisationException {
    choiceRouter.setMuleContext(muleContext);
    choiceRouter.initialise();
  }

  private CoreEvent fooEvent() throws ZernovaException {
    return eventBuilder(muleContext).message(of("foo")).build();
  }

  private CoreEvent zapEvent() throws ZernovaException {
    return eventBuilder(muleContext).message(of("zap")).build();
  }

  private String payloadZapExpression() {
    return "payload == 'zap'";
  }

}

