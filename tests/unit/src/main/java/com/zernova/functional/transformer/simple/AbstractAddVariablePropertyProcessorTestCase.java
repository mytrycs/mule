/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.transformer.simple;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.SET_VARIABLE_WITH_NULL_VALUE;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.api.metadata.MediaType.ANY;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JAVA;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_XML;
import static com.zernova.tck.junit4.matcher.DataTypeMatcher.like;
import static com.zernova.tck.util.ZernovaContextUtils.eventBuilder;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.util.func.CheckedRunnable;
import com.zernova.runtime.core.privileged.processor.simple.AbstractAddVariablePropertyProcessor;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

import jakarta.activation.MimeTypeParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractAddVariablePropertyProcessorTestCase extends AbstractZernovaContextTestCase {

  public static final Charset ENCODING = US_ASCII;
  public static final String PLAIN_STRING_KEY = "someText";
  public static final String PLAIN_STRING_VALUE = "someValue";
  public static final String EXPRESSION = "#['someValue']";
  public static final String NULL_EXPRESSION = "#[null]";
  public static final Charset CUSTOM_ENCODING = UTF_8;

  @Mock(lenient = true)
  private StreamingManager streamingManager;

  private CoreEvent event;
  private Message message;
  private final AbstractAddVariablePropertyProcessor addVariableProcessor;
  private CheckedRunnable afterAssertions;

  public AbstractAddVariablePropertyProcessorTestCase(AbstractAddVariablePropertyProcessor abstractAddVariableProcessor) {
    addVariableProcessor = abstractAddVariableProcessor;
  }

  @Before
  public void setUpTest() throws Exception {
    when(streamingManager.manage(any(CursorProvider.class), any(EventContext.class))).thenAnswer(inv -> inv.getArgument(0));

    addVariableProcessor.setExpressionManager(muleContext.getExpressionManager());
    addVariableProcessor.setStreamingManager(streamingManager);
    addVariableProcessor.setArtifactEncoding(() -> defaultCharset());

    message = of("");
    event = createTestEvent(message);

    afterAssertions = () -> verify(streamingManager, never()).manage(any(CursorProvider.class), any(EventContext.class));

    FeatureFlaggingService featureFlaggingService = mock(FeatureFlaggingService.class);
    Field f1 = addVariableProcessor.getClass().getSuperclass().getDeclaredField("featureFlaggingService");
    f1.setAccessible(true);
    f1.set(addVariableProcessor, featureFlaggingService);
    when(featureFlaggingService.isEnabled(SET_VARIABLE_WITH_NULL_VALUE)).thenReturn(true);
  }

  @After
  public void after() {
    if (afterAssertions != null) {
      afterAssertions.run();
    }
  }

  protected CoreEvent createTestEvent(Message message) throws ZernovaException {
    return eventBuilder(muleContext).message(message).build();
  }

  @Test
  public void testAddVariable() throws ZernovaException {
    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue(PLAIN_STRING_VALUE);
    addVariableProcessor.initialise();
    event = addVariableProcessor.process(event);

    verifyAdded(event, PLAIN_STRING_KEY, PLAIN_STRING_VALUE);
    assertThat(getVariableDataType(event, PLAIN_STRING_KEY), like(String.class, ANY, defaultCharset()));
  }

  @Test
  public void testAddVariableWithExpressionValue() throws ZernovaException {
    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue(EXPRESSION);
    addVariableProcessor.initialise();
    event = addVariableProcessor.process(event);

    verifyAdded(event, PLAIN_STRING_KEY, PLAIN_STRING_VALUE);
    assertThat(getVariableDataType(event, PLAIN_STRING_KEY),
               like(String.class, APPLICATION_JAVA, defaultCharset()));
  }

  @Test
  public void testAddVariableWithExpressionKey() throws ZernovaException {
    addVariableProcessor.setIdentifier(EXPRESSION);
    addVariableProcessor.setValue(PLAIN_STRING_VALUE);
    addVariableProcessor.initialise();
    event = addVariableProcessor.process(event);

    verifyAdded(event, PLAIN_STRING_VALUE, PLAIN_STRING_VALUE);
    assertThat(getVariableDataType(event, PLAIN_STRING_VALUE), like(String.class, ANY, defaultCharset()));
  }

  @Test
  public void testAddVariableWithEncoding() throws ZernovaException {
    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue(PLAIN_STRING_VALUE);
    addVariableProcessor.initialise();
    addVariableProcessor.setReturnDataType(DataType.builder().charset(CUSTOM_ENCODING).build());
    event = addVariableProcessor.process(event);

    verifyAdded(event, PLAIN_STRING_KEY, PLAIN_STRING_VALUE);
    assertThat(getVariableDataType(event, PLAIN_STRING_KEY), like(String.class, ANY, CUSTOM_ENCODING));
  }

  @Test
  public void testAddVariableWithMimeType() throws MimeTypeParseException, ZernovaException {
    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue(PLAIN_STRING_VALUE);
    addVariableProcessor.initialise();
    addVariableProcessor.setReturnDataType(DataType.builder().mediaType(APPLICATION_XML).build());
    event = addVariableProcessor.process(event);

    verifyAdded(event, PLAIN_STRING_KEY, PLAIN_STRING_VALUE);
    assertThat(getVariableDataType(event, PLAIN_STRING_KEY),
               like(String.class, APPLICATION_XML, defaultCharset()));
  }

  protected abstract DataType getVariableDataType(CoreEvent event, String key);

  @Test(expected = IllegalArgumentException.class)
  public void testAddVariableWithNullKey() {
    addVariableProcessor.setIdentifier(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddVariableWithEmptyKey() {
    addVariableProcessor.setIdentifier("");
  }

  @Test(expected = NullPointerException.class)
  public void testAddVariableWithNullValue() {
    addVariableProcessor.setValue(null);
  }

  @Test
  public void testAddVariableWithNullExpressionKeyResult() throws ZernovaException {
    addVariableProcessor.setIdentifier(NULL_EXPRESSION);
    addVariableProcessor.setValue(PLAIN_STRING_VALUE);
    addVariableProcessor.initialise();
    event = addVariableProcessor.process(event);
    verifyNotAdded(event);
  }

  @Test
  public void testAddVariableWithNullExpressionValueResult() throws ZernovaException {
    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue(NULL_EXPRESSION);
    addVariableProcessor.initialise();
    verifyRemoved(event, PLAIN_STRING_KEY);
  }

  @Test
  public void testAddVariableWithNullPayloadExpressionValueResult() throws ZernovaException {
    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue(NULL_EXPRESSION);
    addVariableProcessor.initialise();

    event = addVariableProcessor.process(event);

    verifyAdded(event, PLAIN_STRING_KEY, null);
  }

  @Test
  public void testCursorProvidersAreManaged() throws ZernovaException {
    CursorProvider cursorProvider = mock(CursorProvider.class);

    addVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    addVariableProcessor.setValue("#[payload]");
    addVariableProcessor.initialise();
    event = addVariableProcessor.process(CoreEvent.builder(event).message(of(cursorProvider)).build());

    assertThat(event.getVariables().get(PLAIN_STRING_KEY).getValue(), is(cursorProvider));
    verify(streamingManager).manage(same(cursorProvider), any(EventContext.class));

    afterAssertions = null;
  }

  protected abstract void verifyAdded(CoreEvent event, String key, String value);

  protected abstract void verifyNotAdded(CoreEvent mockEvent);

  protected abstract void verifyRemoved(CoreEvent mockEvent, String key);

}
