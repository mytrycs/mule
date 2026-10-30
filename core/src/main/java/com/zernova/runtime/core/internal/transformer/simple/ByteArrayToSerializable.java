/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import static com.zernova.runtime.api.serialization.ObjectSerializer.DEFAULT_OBJECT_SERIALIZER_NAME;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.transformFailed;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.serialization.SerializationProtocol;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;
import com.zernova.runtime.core.api.transformer.DiscoverableTransformer;
import com.zernova.runtime.core.api.transformer.TransformerException;

import java.io.InputStream;
import java.nio.charset.Charset;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * <code>ByteArrayToSerializable</code> converts a serialized object to its object representation
 */
public class ByteArrayToSerializable extends AbstractTransformer implements DiscoverableTransformer {

  private ObjectSerializer objectSerializer;

  /**
   * Give core transformers a slightly higher priority
   */
  private int priorityWeighting = DiscoverableTransformer.DEFAULT_PRIORITY_WEIGHTING + 1;

  public ByteArrayToSerializable() {
    registerSourceType(DataType.BYTE_ARRAY);
    registerSourceType(DataType.INPUT_STREAM);
    registerSourceType(DataType.CURSOR_STREAM_PROVIDER);
  }

  @Override
  public Object doTransform(Object src, Charset encoding) throws TransformerException {
    SerializationProtocol externalProtocol = getSerializationProtocol();
    try {
      final Object result;
      if (src instanceof byte[]) {
        result = externalProtocol.deserialize((byte[]) src);
      } else if (src instanceof CursorStreamProvider) {
        result = externalProtocol.deserialize(((CursorStreamProvider) src).openCursor());
      } else {
        result = externalProtocol.deserialize((InputStream) src);
      }
      return result;
    } catch (Exception e) {
      throw new TransformerException(transformFailed("byte[]", "Object"), this, e);
    }
  }

  protected SerializationProtocol getSerializationProtocol() {
    return objectSerializer.getExternalProtocol();
  }

  @Override
  public int getPriorityWeighting() {
    return priorityWeighting;
  }

  @Override
  public void setPriorityWeighting(int priorityWeighting) {
    this.priorityWeighting = priorityWeighting;
  }

  @Inject
  @Named(DEFAULT_OBJECT_SERIALIZER_NAME)
  public void setObjectSerializer(ObjectSerializer objectSerializer) {
    this.objectSerializer = objectSerializer;
  }

}
