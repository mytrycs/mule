/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.streaming;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.streaming.object.CursorIterator;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.DefaultStreamingManager;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import com.zernova.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import com.zernova.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;
import com.zernova.runtime.core.internal.streaming.object.factory.InMemoryCursorIteratorProviderFactory;
import com.zernova.runtime.extension.api.runtime.streaming.StreamingHelper;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

@SmallTest
public class DefaultStreamingHelperTestCase extends AbstractZernovaContextTestCase {

  private StreamingHelper streamingHelper;
  private StreamingManager streamingManager;
  private CursorProviderFactory cursorProviderFactory;
  private CoreEvent event;

  private final List<String> valueList = Arrays.asList("Apple", "Banana", "Kiwi");

  @Override
  protected void doSetUp() throws Exception {
    streamingManager = new DefaultStreamingManager();
    initialiseIfNeeded(streamingManager, true, muleContext);
    cursorProviderFactory =
        new InMemoryCursorIteratorProviderFactory(InMemoryCursorIteratorConfig.getDefault(), streamingManager);
    event = testEvent();

    final Pair<CursorStreamProviderFactory, CursorIteratorProviderFactory> cursorProviderFactories =
        streamingManager.getPairFor(cursorProviderFactory);

    streamingHelper = new DefaultStreamingHelper(cursorProviderFactories.getFirst(), cursorProviderFactories.getSecond(),
                                                 event, from("log"));
  }

  @Override
  protected void doTearDown() throws Exception {
    if (streamingManager != null) {
      ((Disposable) streamingManager).dispose();
    }
  }

  @Test
  public void resolveIteratorProvider() {
    CursorIteratorProvider streamProvider = (CursorIteratorProvider) streamingHelper.resolveCursorProvider(valueList.iterator());
    CursorIterator cursor = streamProvider.openCursor();

    valueList.forEach(value -> {
      assertThat(cursor.hasNext(), is(true));
      assertThat(value, equalTo(cursor.next()));
    });

    assertThat(cursor.hasNext(), is(false));
  }

  @Test
  public void resolveStreamableTypedValueProvider() {
    TypedValue typedValue = new TypedValue(new ByteArrayInputStream("Apple".getBytes()), DataType.INPUT_STREAM);
    TypedValue repeatableTypedValue = (TypedValue) streamingHelper.resolveCursorProvider(typedValue);

    assertThat(repeatableTypedValue.getValue(), instanceOf(CursorProvider.class));
  }

  @Test
  public void resolveNonStreamableTypedValueProvider() {
    TypedValue typedValue = new TypedValue("Apple", DataType.STRING);
    TypedValue repeatableTypedValue = (TypedValue) streamingHelper.resolveCursorProvider(typedValue);

    assertThat(repeatableTypedValue.getValue(), not(instanceOf(CursorProvider.class)));
  }

}
