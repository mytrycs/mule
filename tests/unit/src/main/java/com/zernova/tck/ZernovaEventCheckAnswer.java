/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ZernovaEventCheckAnswer extends Object implements Answer<CoreEvent> {

  private CoreEvent returnEvent;
  private ZernovaException exceptionToThrow;

  public ZernovaEventCheckAnswer() {
    this((CoreEvent) null);
  }

  public ZernovaEventCheckAnswer(CoreEvent returnEvent) {
    this.returnEvent = returnEvent;
  }

  public ZernovaEventCheckAnswer(ZernovaException ex) {
    this.exceptionToThrow = ex;
  }

  @Override
  public CoreEvent answer(InvocationOnMock invocation) throws Throwable {
    if (exceptionToThrow != null) {
      throw exceptionToThrow;
    } else {
      return checkInvocation(invocation);
    }
  }

  private CoreEvent checkInvocation(InvocationOnMock invocation) {
    Object[] arguments = invocation.getArguments();
    assertEquals(1, arguments.length);
    assertTrue(arguments[0] instanceof CoreEvent);

    return returnEvent;
  }
}
