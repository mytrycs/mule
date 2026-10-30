/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.event;

import static com.zernova.runtime.core.internal.event.EventQuickCopy.quickCopy;

import static java.util.Collections.singletonMap;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

import io.qameta.allure.Issue;

public class DefaultEventBuilderTestCase extends AbstractZernovaTestCase {

  private static final int NEW_EVENT_CREATIONS = 10000;
  private static final String INTERNAL_PARAMETER_KEY = "key";
  private static final String INTERNAL_PARAMETER_VALUE = "value";
  private static final String CORRELATION_ID = "correlationId";

  @Test
  @Issue("MULE-19180")
  public void stackOverflowOverInternalParameter() throws ZernovaException {
    InternalEvent quickCopy = quickCopy(newEvent(), singletonMap(INTERNAL_PARAMETER_KEY, INTERNAL_PARAMETER_VALUE));

    // Setting correlation to force a new event instance
    InternalEvent event = new DefaultEventBuilder(quickCopy).correlationId(CORRELATION_ID).build();
    for (int i = 0; i < NEW_EVENT_CREATIONS; i++) {
      // Setting correlation to force a new event instance
      event = new DefaultEventBuilder(event).correlationId(CORRELATION_ID).build();
    }
    assertThat(event.getInternalParameters().size(), is(1));
    assertThat(event.getInternalParameters().keySet(), hasItem(INTERNAL_PARAMETER_KEY));
  }
}
