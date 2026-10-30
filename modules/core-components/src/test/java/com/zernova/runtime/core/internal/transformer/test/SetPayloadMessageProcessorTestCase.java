/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.test;

import static java.nio.charset.StandardCharsets.UTF_16;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.tck.junit4.matcher.DataTypeMatcher.like;
import static com.zernova.test.allure.AllureConstants.ComponentsFeature.CORE_COMPONENTS;
import static com.zernova.test.allure.AllureConstants.ComponentsFeature.SetPayloadStory.SET_PAYLOAD;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.processor.simple.SetPayloadMessageProcessor;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(CORE_COMPONENTS)
@Story(SET_PAYLOAD)
public class SetPayloadMessageProcessorTestCase extends AbstractZernovaContextTestCase {

  private static final String PLAIN_TEXT = "This is a plain text";
  private static final String EXPRESSION = "#[vars.testVariable]";
  private static final Charset CUSTOM_ENCODING = UTF_16;

  private SetPayloadMessageProcessor setPayloadMessageProcessor;

  @Before
  public void setUp() throws Exception {
    setPayloadMessageProcessor = new SetPayloadMessageProcessor();
    setPayloadMessageProcessor.setExpressionManager(muleContext.getExpressionManager());
  }

  @Test
  public void setsNullPayload() throws ZernovaException {
    setPayloadMessageProcessor.setValue(null);
    setPayloadMessageProcessor.initialise();

    CoreEvent response = setPayloadMessageProcessor.process(testEvent());

    assertThat(response.getMessage().getPayload().getValue(), is(nullValue()));
  }

  @Test
  public void setsPlainText() throws ZernovaException {
    setPayloadMessageProcessor.setValue(PLAIN_TEXT);
    setPayloadMessageProcessor.initialise();

    CoreEvent response = setPayloadMessageProcessor.process(testEvent());
    assertThat(response.getMessage().getPayload().getValue(), is(PLAIN_TEXT));
  }

  @Test
  public void setsExpressionPayload() throws ZernovaException {
    setPayloadMessageProcessor.setValue(EXPRESSION);
    setPayloadMessageProcessor.initialise();


    CoreEvent response = setPayloadMessageProcessor.process(CoreEvent.builder(testEvent())
        .addVariable("testVariable", PLAIN_TEXT, STRING)
        .build());

    assertThat(response.getMessage().getPayload().getValue(), is(PLAIN_TEXT));
  }

  @Test
  public void setsDefaultDataTypeForNullPayload() throws ZernovaException {
    setPayloadMessageProcessor.setValue(null);
    setPayloadMessageProcessor.initialise();

    CoreEvent response = setPayloadMessageProcessor.process(testEvent());

    assertThat(response.getMessage().getPayload().getDataType(), like(Object.class, MediaType.ANY, null));
  }

  @Test
  public void setsDefaultDataTypeForNonNullValue() throws ZernovaException {
    setPayloadMessageProcessor.setValue(PLAIN_TEXT);
    setPayloadMessageProcessor.initialise();

    setPayloadMessageProcessor.process(testEvent());

    assertThat(testEvent().getMessage().getPayload().getDataType(), like(String.class, MediaType.ANY, null));
  }

  @Test
  public void setsCustomEncoding() throws ZernovaException {
    setPayloadMessageProcessor.setValue(PLAIN_TEXT);
    setPayloadMessageProcessor.setDataType(DataType.builder().charset(CUSTOM_ENCODING).build());
    setPayloadMessageProcessor.initialise();

    CoreEvent response = setPayloadMessageProcessor.process(testEvent());

    assertThat(response.getMessage().getPayload().getDataType(), like(String.class, MediaType.ANY, CUSTOM_ENCODING));
  }

  @Test
  public void setsCustomMimeType() throws ZernovaException {
    setPayloadMessageProcessor.setValue(PLAIN_TEXT);
    setPayloadMessageProcessor.setDataType(DataType.builder().mediaType(MediaType.APPLICATION_XML).build());
    setPayloadMessageProcessor.initialise();

    CoreEvent response = setPayloadMessageProcessor.process(testEvent());

    assertThat(response.getMessage().getPayload().getDataType(), like(String.class, MediaType.APPLICATION_XML, null));
  }
}
