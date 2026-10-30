/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import static com.zernova.runtime.api.metadata.DataType.BYTE_ARRAY;
import static com.zernova.runtime.api.metadata.DataType.INPUT_STREAM;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.TransfromersStory.TRANSFORMERS;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;
import com.zernova.runtime.core.api.transformer.DiscoverableTransformer;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.transformer.TransformersRegistry;
import com.zernova.runtime.core.internal.transformer.simple.InputStreamToByteArray;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.io.FilterInputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(REGISTRY)
@Story(TRANSFORMERS)
public class TransformerCachingTestCase extends AbstractZernovaContextTestCase {

  private TransformersRegistry registry;

  @Before
  public void before() throws RegistrationException {
    registry = ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(TransformersRegistry.class);
  }

  @Test
  public void testCacheUpdate() throws Exception {
    DataType sourceType = DataType.fromType(FilterInputStream.class);
    Transformer trans = registry.lookupTransformer(sourceType, BYTE_ARRAY);
    assertNotNull(trans);
    assertTrue(trans instanceof InputStreamToByteArray);

    Transformer trans2 = new FilterInputStreamToByteArray();
    registry.registerTransformer(trans2);

    trans = registry.lookupTransformer(sourceType, BYTE_ARRAY);
    assertNotNull(trans);
    assertTrue(trans instanceof FilterInputStreamToByteArray);

    trans = registry.lookupTransformer(INPUT_STREAM, BYTE_ARRAY);
    assertNotNull(trans);
    assertTrue(trans instanceof InputStreamToByteArray);
  }

  public static class FilterInputStreamToByteArray extends AbstractTransformer implements DiscoverableTransformer {

    public FilterInputStreamToByteArray() {
      registerSourceType(DataType.fromType(FilterInputStream.class));
      setReturnDataType(BYTE_ARRAY);
    }

    @Override
    protected Object doTransform(Object src, Charset outputEncoding) throws TransformerException {
      throw new UnsupportedOperationException("This is a transformer only to be used for testing");
    }

    @Override
    public int getPriorityWeighting() {
      return 0;
    }

    @Override
    public void setPriorityWeighting(int weighting) {
      // no-op
    }
  }
}
