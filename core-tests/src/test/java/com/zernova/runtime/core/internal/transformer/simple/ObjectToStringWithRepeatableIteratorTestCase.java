/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.TO_STRING_TRANSFORMER_TRANSFORM_ITERATOR_ELEMENTS;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.internal.streaming.object.ListCursorIterator;
import com.zernova.tck.core.transformer.AbstractTransformerTestCase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

import io.qameta.allure.Issue;

@Issue("MULE-19323")
public class ObjectToStringWithRepeatableIteratorTestCase extends AbstractTransformerTestCase {

  private FeatureFlaggingService featureFlaggingService;

  @Before
  public void setUp() {
    featureFlaggingService = mock(FeatureFlaggingService.class);
    when(featureFlaggingService.isEnabled(TO_STRING_TRANSFORMER_TRANSFORM_ITERATOR_ELEMENTS)).thenReturn(true);
  }

  @Override
  public Transformer getTransformer() throws Exception {
    final ObjectToString objectToString = configureTransformer(new ObjectToString());
    objectToString.setFeatureFlags(featureFlaggingService);
    return objectToString;
  }

  @Override
  public Object getTestData() {
    List<String> list = new ArrayList<>();
    list.add("one");
    list.add(null);
    list.add("three");

    final CursorIteratorProvider iteratorProvider = mock(CursorIteratorProvider.class);
    when(iteratorProvider.openCursor()).thenReturn(new ListCursorIterator<>(iteratorProvider, list));

    return iteratorProvider;
  }

  @Override
  public Object getResultData() {
    return "[one, null, three]";
  }

  @Override
  public Transformer getRoundTripTransformer() throws Exception {
    // we do not want round trip transforming tested
    return null;
  }
}
