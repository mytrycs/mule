/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.internal.streaming.CursorUtils.unwrap;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.STREAMING;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.StreamingStory.STREAM_MANAGEMENT;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

@Feature(STREAMING)
@Story(STREAM_MANAGEMENT)
public class CursorUtilTestCase extends AbstractZernovaTestCase {

  @Test
  @Issue("MULE-18573")
  public void unwrapNonDecoratedProvider() {
    CursorProvider provider = mock(CursorProvider.class);
    assertThat(unwrap(provider), is(sameInstance(provider)));
  }

  @Test
  @Issue("MULE-18573")
  public void unwrapOneLevelDecorator() {
    CursorProvider provider = mock(CursorProvider.class);
    CursorProviderDecorator decorator = mock(CursorProviderDecorator.class);
    when(decorator.getDelegate()).thenReturn(provider);

    assertThat(unwrap(decorator), is(sameInstance(provider)));
  }

  @Test
  @Issue("MULE-18573")
  public void unwrap42thLevelDecorator() {
    CursorProvider provider = mock(CursorProvider.class);
    CursorProviderDecorator decorator = mock(CursorProviderDecorator.class);
    when(decorator.getDelegate()).thenReturn(provider);

    for (int i = 0; i < 41; i++) {
      CursorProviderDecorator newDecorator = mock(CursorProviderDecorator.class);
      when(newDecorator.getDelegate()).thenReturn(decorator);
      decorator = newDecorator;
    }

    assertThat(unwrap(decorator), is(sameInstance(provider)));
  }
}
