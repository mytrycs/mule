/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.result;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class ValueReturnDelegateTestCase extends ValueReturnDelegateContractTestCase {

  @Override
  protected ReturnDelegate createReturnDelegate() throws InitialisationException {
    return new ValueReturnDelegate(componentModel, artifactEncoding);
  }

  @Override
  protected Message getOutputMessage(CoreEvent result) {
    Message message = result.getMessage();

    assertThat(message, is(notNullValue()));
    return message;
  }

  @Test
  public void evaluateEvent() {
    CoreEvent event = mock(CoreEvent.class);
    assertThat(delegate.asReturnValue(event, operationContext), is(sameInstance(event)));
  }
}
