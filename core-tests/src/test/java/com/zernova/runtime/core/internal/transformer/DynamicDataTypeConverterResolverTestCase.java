/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer;

import static com.zernova.runtime.api.metadata.DataType.BYTE_ARRAY;
import static com.zernova.runtime.api.metadata.DataType.INPUT_STREAM;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.TransfromersStory.TRANSFORMERS;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.internal.transformer.builder.MockConverterBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SmallTest
@Feature(REGISTRY)
@Story(TRANSFORMERS)
public class DynamicDataTypeConverterResolverTestCase extends AbstractZernovaTestCase {

  private final ZernovaContextWithRegistry muleContext = mock(ZernovaContextWithRegistry.class);
  private final ZernovaRegistry muleRegistry = mock(ZernovaRegistry.class);
  private final TransformersRegistry transformersRegistry = mock(TransformersRegistry.class);

  @Test
  public void doesNotFailWhenThereIsNoDataTypeResolution() throws TransformerException {
    when(muleContext.getRegistry()).thenReturn(muleRegistry);
    when(transformersRegistry.lookupTransformer(any(DataType.class), any(DataType.class))).thenReturn(null);

    DynamicDataTypeConversionResolver resolver = new DynamicDataTypeConversionResolver(transformersRegistry);

    List<DataType> targetValues = new ArrayList<>();
    targetValues.add(DataType.STRING);
    Transformer resolvedConverter = resolver.resolve(INPUT_STREAM, targetValues);

    assertEquals(null, resolvedConverter);
  }

  @Test
  public void findsExpectedConverter() throws TransformerException {
    Transformer expectedConverter = new MockConverterBuilder().from(BYTE_ARRAY).to(STRING).build();

    when(muleContext.getRegistry()).thenReturn(muleRegistry);
    when(transformersRegistry.lookupTransformer(BYTE_ARRAY, STRING)).thenReturn(expectedConverter);

    DynamicDataTypeConversionResolver resolver = new DynamicDataTypeConversionResolver(transformersRegistry);

    List<DataType> targetValues = new ArrayList<>();
    targetValues.add(INPUT_STREAM);
    targetValues.add(STRING);
    Transformer resolvedConverter = resolver.resolve(BYTE_ARRAY, targetValues);

    assertEquals(expectedConverter, resolvedConverter);
  }
}
