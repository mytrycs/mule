/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import static com.zernova.runtime.api.serialization.ObjectSerializer.DEFAULT_OBJECT_SERIALIZER_NAME;

import com.zernova.runtime.api.i18n.I18nMessageFactory;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.core.api.message.OutputHandler;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;
import com.zernova.runtime.core.api.transformer.DiscoverableTransformer;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.api.util.IOUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/** <code>ObjectToOutputHandler</code> converts a byte array into a String. */
public class ObjectToOutputHandler extends AbstractTransformer implements DiscoverableTransformer {

  private ObjectSerializer objectSerializer;

  /** Give core transformers a slighty higher priority */
  private int priorityWeighting = DiscoverableTransformer.DEFAULT_PRIORITY_WEIGHTING + 1;

  public ObjectToOutputHandler() {
    registerSourceType(DataType.BYTE_ARRAY);
    registerSourceType(DataType.STRING);
    registerSourceType(DataType.INPUT_STREAM);
    registerSourceType(DataType.CURSOR_STREAM_PROVIDER);
    registerSourceType(DataType.fromType(Serializable.class));
    setReturnDataType(DataType.fromType(OutputHandler.class));
  }

  @Override
  public Object doTransform(final Object src, final Charset encoding) throws TransformerException {
    if (src instanceof String) {
      return (OutputHandler) (event, out) -> out.write(((String) src).getBytes(encoding));
    } else if (src instanceof byte[]) {
      return (OutputHandler) (event, out) -> out.write((byte[]) src);
    } else if (src instanceof CursorStreamProvider) {
      return handleInputStream(((CursorStreamProvider) src).openCursor());
    } else if (src instanceof InputStream) {
      return handleInputStream((InputStream) src);
    } else if (src instanceof Serializable) {
      return (OutputHandler) (event, out) -> objectSerializer.getExternalProtocol().serialize(src, out);
    } else {
      throw new TransformerException(I18nMessageFactory
          .createStaticMessage("Unable to convert " + src.getClass() + " to OutputHandler."));
    }
  }

  private OutputHandler handleInputStream(InputStream is) {
    return (event, out) -> {
      try {
        IOUtils.copyLarge(is, out);
      } finally {
        is.close();
      }
    };
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
