/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static com.zernova.runtime.api.notification.PolicyNotification.AFTER_NEXT;

import static java.util.Collections.emptyMap;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.component.location.LocationPart;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.privileged.event.DefaultFlowCallStack;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class OnExecuteNextErrorConsumerTestCase extends AbstractZernovaTestCase {

  private static final String EVENT_ID = "eventId";

  private OnExecuteNextErrorConsumer consumer;

  private InternalEvent initialEvent;
  private InternalEvent updatedEvent;
  private PolicyNotificationHelper notificationHelper;
  private ComponentLocation location;
  private DefaultFlowCallStack flowCallStack;

  @Before
  public void setUp() {
    initialEvent = mock(InternalEvent.class, RETURNS_DEEP_STUBS);
    updatedEvent = mock(InternalEvent.class, RETURNS_DEEP_STUBS);
    notificationHelper = mock(PolicyNotificationHelper.class);
    location = mock(ComponentLocation.class);
    flowCallStack = mock(DefaultFlowCallStack.class);
    Function<MessagingException, CoreEvent> prepareEvent = event -> updatedEvent;

    consumer = new OnExecuteNextErrorConsumer(prepareEvent, notificationHelper, location, emptyMap());
  }

  @Test
  public void notificationWithUpdatedEevenIsFired() {
    MessagingException messagingException = mock(MessagingException.class);

    when(initialEvent.getContext().getId()).thenReturn(EVENT_ID);
    when(updatedEvent.getFlowCallStack()).thenReturn(flowCallStack);
    when(messagingException.getEvent()).thenReturn(initialEvent);
    when(location.getParts()).thenReturn(newArrayList(mock(LocationPart.class), mock(LocationPart.class)));

    consumer.accept(messagingException);

    verify(notificationHelper).fireNotification(updatedEvent, messagingException, AFTER_NEXT);
    verify(flowCallStack).push(any());
  }
}
