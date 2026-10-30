/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.result;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class VoidReturnDelegateTestCase extends AbstractZernovaTestCase {

  @Test
  public void returnsMuleEvent() throws ZernovaException {
    CoreEvent event = newEvent();
    ExecutionContextAdapter operationContext = mock(ExecutionContextAdapter.class);
    when(operationContext.getEvent()).thenReturn(event);

    Object returnValue = VoidReturnDelegate.INSTANCE.asReturnValue(new Object(), operationContext);
    assertThat(event, is(sameInstance(returnValue)));
  }
}
