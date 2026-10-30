/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.result;

import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JSON;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.internal.streaming.CursorUtils.unwrap;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.streaming.ManagedCursorProvider;
import com.zernova.runtime.core.internal.streaming.bytes.ManagedCursorStreamProvider;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.tck.size.SmallTest;
import com.zernova.weave.v2.el.ByteArrayBasedCursorStreamProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import org.junit.After;
import org.junit.Test;

import io.qameta.allure.Issue;

@SmallTest
public class TargetReturnDelegateTestCase extends ValueReturnDelegateTestCase {

  protected static final String TARGET = "myFlowVar";

  @Override
  protected ReturnDelegate createReturnDelegate() {
    return new TargetReturnDelegate(TARGET, "#[message]", componentModel,
                                    muleContext.getExpressionManager(),
                                    artifactEncoding, streamingManager);
  }

  @After
  public void after() {
    assertThat(event.getMessage().getPayload().getValue(), is(""));
  }

  @Override
  protected Message getOutputMessage(CoreEvent result) {
    Message message = (Message) result.getVariables().get(TARGET).getValue();

    assertThat(message, is(notNullValue()));
    return message;
  }

  @Override
  @Test
  public void evaluateEvent() {
    CoreEvent event = mock(CoreEvent.class);
    final String payload = "Hello there!";
    Message message = Message.of(payload);
    when(event.getMessage()).thenReturn(message);

    CoreEvent resultEvent = delegate.asReturnValue(event, operationContext);
    Message resultMessage = getOutputMessage(resultEvent);
    assertThat(resultMessage.getPayload().getValue(), is(payload));
  }


  @Test
  @Issue("MULE-19068")
  public void targetReturnDelegateShouldManageCursorStreamProvider() throws IOException {
    when(componentModel.supportsStreaming()).thenReturn(true);

    delegate = new TargetReturnDelegate(TARGET, "#[payload.token]", componentModel,
                                        muleContext.getExpressionManager(),
                                        artifactEncoding, streamingManager);

    MediaType mediaType = APPLICATION_JSON.withCharset(Charset.defaultCharset());
    Result<Object, Object> value = Result.builder()
        .output(IOUtils.toInputStream("{\"token\": \"test-token\", \"id\": \"sampleid\"}")).mediaType(mediaType).build();

    CoreEvent result = delegate.asReturnValue(value, operationContext);

    assertThat(result.getVariables().get(TARGET).getValue(), is(instanceOf(ManagedCursorProvider.class)));
    ManagedCursorStreamProvider cursorStreamProvider =
        (ManagedCursorStreamProvider) result.getVariables().get(TARGET).getValue();
    assertThat(unwrap(cursorStreamProvider), is(instanceOf(ByteArrayBasedCursorStreamProvider.class)));
    InputStream inputStream = cursorStreamProvider.openCursor();
    assertThat(IOUtils.toString(inputStream), is("\"test-token\""));
  }
}
