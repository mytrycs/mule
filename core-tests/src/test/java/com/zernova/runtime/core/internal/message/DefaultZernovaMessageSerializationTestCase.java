/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.message;

import static com.zernova.runtime.core.internal.context.DefaultZernovaContext.currentMuleContext;
import static com.zernova.test.allure.AllureConstants.SerializationFeature.SERIALIZATION;
import static com.zernova.test.allure.AllureConstants.SerializationFeature.SerializationStory.MESSAGE_SERIALIZATION;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.transformer.TransformersRegistry;
import com.zernova.runtime.core.internal.transformer.simple.ObjectToByteArray;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.lang3.SerializationUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SERIALIZATION)
@Story(MESSAGE_SERIALIZATION)
public class DefaultZernovaMessageSerializationTestCase extends AbstractZernovaContextTestCase {

  private static final String INNER_TEST_MESSAGE = "TestTestTestHello";

  @Before
  public void setUp() {
    currentMuleContext.set(muleContext);
  }

  @After
  public void tearDown() {
    currentMuleContext.set(null);
  }

  @Test
  public void testSerializablePayload() throws Exception {
    final Message message = InternalMessage.builder().value(TEST_MESSAGE).build();
    Message deserializedMessage = serializationRoundtrip(message);

    assertEquals(TEST_MESSAGE, deserializedMessage.getPayload().getValue());
  }

  @Test
  public void testNonSerializablePayload() throws Exception {
    // add a transformer to the registry that can convert a NonSerializable to byte[]. This
    // will be used during Serialization
    ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(TransformersRegistry.class)
        .registerTransformer(new NonSerializableToByteArray());

    final Message message = InternalMessage.builder().value(new NonSerializable()).build();

    InternalMessage deserializedMessage = serializationRoundtrip(message);

    assertTrue(deserializedMessage.getPayload().getValue() instanceof byte[]);
    assertEquals(INNER_TEST_MESSAGE, getPayloadAsString(deserializedMessage));
  }

  @Test
  public void testStreamPayloadSerialization() throws Exception {
    InputStream stream = new ByteArrayInputStream(TEST_MESSAGE.getBytes());
    final Message message = InternalMessage.builder().value(stream).build();

    InternalMessage deserializedMessage = serializationRoundtrip(message);
    assertEquals(byte[].class, deserializedMessage.getPayload().getDataType().getType());
    byte[] payload = (byte[]) deserializedMessage.getPayload().getValue();
    assertTrue(Arrays.equals(TEST_MESSAGE.getBytes(), payload));
  }

  @Test
  public void messageSerializationKeepsMediaType() throws Exception {
    final Message message = InternalMessage.builder()
        .payload(new TypedValue<>(new ByteArrayInputStream("{\"id\":\"1\"}".getBytes()), DataType.JSON_STRING)).build();
    InternalMessage deserializedMessage = serializationRoundtrip(message);
    assertThat(deserializedMessage.getPayload().getDataType().getMediaType(),
               is(equalTo(message.getPayload().getDataType().getMediaType())));
  }

  private InternalMessage serializationRoundtrip(Message message) throws Exception {
    return (InternalMessage) SerializationUtils.deserialize(SerializationUtils.serialize(message));
  }

  static class NonSerializable {

    private final String content = INNER_TEST_MESSAGE;

    String getContent() {
      return content;
    }
  }

  static class NonSerializableToByteArray extends ObjectToByteArray {

    public NonSerializableToByteArray() {
      super();
      registerSourceType(DataType.fromType(NonSerializable.class));
      setReturnDataType(DataType.BYTE_ARRAY);
    }

    @Override
    public Object doTransform(Object src, Charset outputEncoding) throws TransformerException {
      String content = ((NonSerializable) src).getContent();
      return content.getBytes();
    }
  }
}
