/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el.function;

import static java.lang.Thread.currentThread;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.test.allure.AllureConstants.ExpressionLanguageFeature.EXPRESSION_LANGUAGE;
import static com.zernova.test.allure.AllureConstants.ExpressionLanguageFeature.ExpressionLanguageStory.SUPPORT_FUNCTIONS;

import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(EXPRESSION_LANGUAGE)
@Story(SUPPORT_FUNCTIONS)
public class LookupFunctionTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expected = none();

  private String flowName;
  private Flow flow;
  private ConfigurationComponentLocator locator;

  private LookupFunction lookup;
  private CompletableFuture<Event> lookupResult;

  @Before
  public void before() throws ZernovaException {
    flowName = "flowName";
    flow = mock(Flow.class);

    when(flow.execute(any(Event.class))).thenAnswer(inv -> lookupResult);

    locator = mock(ConfigurationComponentLocator.class);
    when(locator.find(Location.builder().globalName(flowName).build())).thenReturn(of(flow));

    lookup = new LookupFunction(locator, mock(SchedulerService.class));
    PrivilegedEvent.setCurrentEvent((PrivilegedEvent) testEvent());
  }

  @Test
  public void happyPath() throws ZernovaException {
    lookupResult = completedFuture(CoreEvent.builder(testEvent()).message(Message.of("Goodbye World!")).build());
    final TypedValue result = (TypedValue) lookup.call(new Object[] {flowName, "Hello World!", 1}, NULL_BINDING_CONTEXT);

    assertThat(result.getValue(), equalTo("Goodbye World!"));
  }

  @Test
  public void interruption() throws ZernovaException {
    lookupResult = new CompletableFuture<>();
    currentThread().interrupt();
    assertThat(lookupResult.isCancelled(), is(false));

    try {
      expected.expectCause(instanceOf(InterruptedException.class));
      lookup.call(new Object[] {flowName, "Hello World!", 1}, NULL_BINDING_CONTEXT);
    } finally {
      assertThat(lookupResult.isCancelled(), is(true));
      assertThat(currentThread().isInterrupted(), is(true));
    }
  }

  @Test
  public void timeout() throws ZernovaException {
    lookupResult = new CompletableFuture<>();
    assertThat(lookupResult.isCancelled(), is(false));

    try {
      expected.expectMessage("Flow 'flowName' has timed out after 1 millis");
      expected.expectCause(instanceOf(TimeoutException.class));
      lookup.call(new Object[] {flowName, "Hello World!", 1}, NULL_BINDING_CONTEXT);
    } finally {
      assertThat(lookupResult.isCancelled(), is(true));
    }
  }
}
