/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.command;

import static com.zernova.runtime.tracer.impl.span.command.EventContextInjectDistributedTraceContextCommand.getEventContextInjectDistributedTraceContextCommand;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.DEFAULT_CORE_EVENT_TRACER;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.tracer.api.context.SpanContextAware;
import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;
import com.zernova.runtime.tracer.impl.context.EventSpanContext;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;
import org.slf4j.Logger;

@Feature(PROFILING)
@Story(DEFAULT_CORE_EVENT_TRACER)
public class EventContextInjectDistributedTraceContextCommandTestCase {

  public static final String TEST_ERROR_MESSAGE = "Test error";

  @Test
  public void distributedTraceContextIsInjected() {
    SpanContextAware eventContext = mock(SpanContextAware.class, withSettings().extraInterfaces(EventContext.class));

    DistributedTraceContextGetter getter = mock(DistributedTraceContextGetter.class);

    getEventContextInjectDistributedTraceContextCommand(mock(Logger.class), TEST_ERROR_MESSAGE, true)
        .execute((EventContext) eventContext, getter);

    verify(eventContext).setSpanContext(any(EventSpanContext.class));

  }
}
