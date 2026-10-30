/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core;

import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;

import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.message.ItemSequenceInfo;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.message.GroupCorrelation;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DefaultMessageContextTestCase extends AbstractZernovaTestCase {

  private static final String GENERATED_CORRELATION_ID = "generatedCorrelationIdValue";
  private static final String CUSTOM_CORRELATION_ID = "customCorrelationIdValue";
  private static final String SERVER_ID = "serverId";

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private final ZernovaContext muleContext = mockContextWithServices();

  @Mock(lenient = true)
  private FlowConstruct flow;

  private EventContext executionContext;
  private EventContext executionContextWithCorrelation;

  @Before
  public void before() {
    when(flow.getMuleContext()).thenReturn(muleContext);
    when(flow.getUniqueIdString()).thenReturn(GENERATED_CORRELATION_ID);
    when(flow.getServerId()).thenReturn(SERVER_ID);

    executionContext = create(flow, TEST_CONNECTOR_LOCATION);
    executionContextWithCorrelation = create(flow, TEST_CONNECTOR_LOCATION, CUSTOM_CORRELATION_ID);
  }

  @Test
  public void noCorrelationIdInContext() {
    final Message message = of(TEST_PAYLOAD);
    final CoreEvent event = CoreEvent.builder(executionContext).message(message).build();

    assertThat(event.getCorrelationId(), is(GENERATED_CORRELATION_ID));
  }

  @Test
  public void correlationIdInContext() {
    final Message message = of(TEST_PAYLOAD);
    final CoreEvent event = CoreEvent.builder(executionContextWithCorrelation).message(message).build();

    assertThat(event.getCorrelationId(), is(CUSTOM_CORRELATION_ID));
  }

  @Test
  public void overrideCorrelationIdInContextWithGroupCorrelation() {
    final Message message = of(TEST_PAYLOAD);
    final CoreEvent event = CoreEvent.builder(executionContextWithCorrelation).message(message)
        .groupCorrelation(empty()).build();

    assertThat(event.getCorrelationId(), is(CUSTOM_CORRELATION_ID));
  }

  @Test
  public void overrideCorrelationIdInContextSequenceWithGroupCorrelation() {
    final Message message = of(TEST_PAYLOAD);
    final CoreEvent event =
        PrivilegedEvent.builder(executionContextWithCorrelation).message(message).correlationId(CUSTOM_CORRELATION_ID)
            .groupCorrelation(Optional.of(GroupCorrelation.of(6))).build();

    assertThat(event.getCorrelationId(), is(CUSTOM_CORRELATION_ID));
  }

  @Test
  public void overrideCorrelationIdInContext() {
    final Message message = of(TEST_PAYLOAD);
    final CoreEvent event = CoreEvent.builder(executionContextWithCorrelation).message(message)
        .itemSequenceInfo(empty()).build();

    assertThat(event.getCorrelationId(), is(CUSTOM_CORRELATION_ID));
  }

  @Test
  public void overrideCorrelationIdInContextSequence() {
    final Message message = of(TEST_PAYLOAD);
    final CoreEvent event =
        PrivilegedEvent.builder(executionContextWithCorrelation).message(message).correlationId(CUSTOM_CORRELATION_ID)
            .itemSequenceInfo(Optional.of(ItemSequenceInfo.of(6))).build();

    assertThat(event.getCorrelationId(), is(CUSTOM_CORRELATION_ID));
  }

}
