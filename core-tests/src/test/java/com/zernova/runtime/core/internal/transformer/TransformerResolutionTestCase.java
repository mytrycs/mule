/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer;

import static com.zernova.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.TransfromersStory.TRANSFORMERS;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.internal.registry.TypeBasedTransformerResolver;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Apple;
import com.zernova.tck.testmodels.fruit.Fruit;
import com.zernova.tck.testmodels.fruit.Orange;

import java.nio.charset.Charset;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SmallTest
@Feature(REGISTRY)
@Story(TRANSFORMERS)
public class TransformerResolutionTestCase extends AbstractZernovaTestCase {

  public static final DataType FRUIT_DATA_TYPE = DataType.fromType(Fruit.class);
  public static final DataType ORANGE_DATA_TYPE = DataType.fromType(Orange.class);
  public static final DataType APPLE_DATA_TYPE = DataType.fromType(Apple.class);

  @Test
  public void resolvesMultipleApplicableTransformers() throws ZernovaException {
    DefaultTransformersRegistry registry = new DefaultTransformersRegistry();
    TypeBasedTransformerResolver resolver = new TypeBasedTransformerResolver();
    resolver.setTransformersRegistry(registry);
    registry.setTransformerResolvers(singletonList(resolver));
    registry.setTransformers(asList(new StringToOrange(), new StringToApple(), new StringToFruit()));
    registry.initialise();

    try {
      Transformer transformer = registry.lookupTransformer(DataType.STRING, FRUIT_DATA_TYPE);
      assertThat(format("Expected a %s transformer but got %s", StringToFruit.class.getName(),
                        transformer.getClass().getName()),
                 transformer, instanceOf(StringToFruit.class));
    } catch (TransformerException e) {
      fail("Unable to properly resolve transformer");
    }
  }

  protected class AbstractStringToFruit extends AbstractDiscoverableTransformer {

    public AbstractStringToFruit() {
      registerSourceType(DataType.STRING);
    }

    @Override
    protected Object doTransform(Object src, Charset encoding) throws TransformerException {
      return new Orange();
    }
  }

  protected class StringToFruit extends AbstractStringToFruit {

    public StringToFruit() {
      setReturnDataType(FRUIT_DATA_TYPE);
    }
  }

  protected class StringToOrange extends AbstractStringToFruit {

    public StringToOrange() {
      setReturnDataType(ORANGE_DATA_TYPE);
    }
  }

  protected class StringToApple extends AbstractStringToFruit {

    public StringToApple() {
      setReturnDataType(APPLE_DATA_TYPE);
    }
  }
}
