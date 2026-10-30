/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.connector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;
import com.zernova.tck.core.streaming.DummyByteBufferManager;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class PetStoreSerializableParameterTestCase extends AbstractExtensionFunctionalTestCase {

  private static final String DONKEY = "donkey";

  @Override
  protected String getConfigFile() {
    return "petstore-serializable-parameter.xml";
  }

  @Test
  public void staticSerializableParameter() throws Exception {
    assertThat(flowRunner("staticSerializableParameter").run().getMessage().getPayload().getValue(), is(DONKEY));
  }

  @Test
  public void dynamicSerializableParameter() throws Exception {
    Message message = flowRunner("dynamicSerializableParameter").withVariable("animal", DONKEY).run().getMessage();
    assertThat(message.getPayload().getValue(), is(DONKEY));
  }

  @Test
  public void inputStreamParameter() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(DONKEY.getBytes());
    Message message = flowRunner("dynamicSerializableParameter").withVariable("animal", inputStream).run().getMessage();
    assertThat(message.getPayload().getValue(), is(DONKEY));
  }

  @Test
  public void cursorStreamProviderParameter() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(DONKEY.getBytes());
    CursorStreamProvider provider =
        new InMemoryCursorStreamProvider(inputStream, InMemoryCursorStreamConfig.getDefault(), new DummyByteBufferManager());
    Message message = flowRunner("dynamicSerializableParameter").withVariable("animal", provider).run().getMessage();
    assertThat(message.getPayload().getValue(), is(DONKEY));
  }
}
