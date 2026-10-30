/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.transformer.simple;

import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;
import com.zernova.runtime.core.internal.transformer.simple.ByteArrayToSerializable;
import com.zernova.runtime.core.internal.transformer.simple.SerializableToByteArray;
import com.zernova.tck.core.transformer.AbstractTransformerTestCase;
import com.zernova.tck.testmodels.fruit.Orange;

import org.apache.commons.lang3.SerializationUtils;

import org.junit.Before;

public class SerialisedObjectTransformersTestCase extends AbstractTransformerTestCase {

  protected ObjectSerializer objectSerializer;

  private Orange testObject = new Orange(Integer.valueOf(4), Double.valueOf(14.3), "nice!");

  @Before
  public void setUp() throws Exception {
    objectSerializer = new JavaObjectSerializer(this.getClass().getClassLoader());
  }

  @Override
  public Transformer getTransformer() throws Exception {
    SerializableToByteArray transformer = configureTransformer(new SerializableToByteArray());
    transformer.setObjectSerializer(objectSerializer);
    return transformer;
  }

  @Override
  public Transformer getRoundTripTransformer() throws Exception {
    ByteArrayToSerializable transformer = configureTransformer(new ByteArrayToSerializable());
    transformer.setObjectSerializer(objectSerializer);
    return transformer;
  }

  @Override
  public Object getTestData() {
    return testObject;
  }

  @Override
  public Object getResultData() {
    return SerializationUtils.serialize(testObject);
  }

}
