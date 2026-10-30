/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.TO_STRING_TRANSFORMER_TRANSFORM_ITERATOR_ELEMENTS;
import static com.zernova.runtime.api.meta.ZernovaVersion.v4_4_0;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.errorReadingStream;
import static com.zernova.runtime.core.api.util.IOUtils.copyLarge;
import static com.zernova.runtime.core.privileged.event.PrivilegedEvent.getCurrentEvent;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.config.ZernovaRuntimeFeature;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.api.streaming.object.CursorIterator;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.api.config.FeatureFlaggingRegistry;
import com.zernova.runtime.core.api.message.OutputHandler;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;
import com.zernova.runtime.core.api.transformer.DiscoverableTransformer;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.api.util.StringMessageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import jakarta.inject.Inject;

/**
 * <code>ObjectToString</code> transformer is useful for debugging. It will return human-readable output for various kinds of
 * objects. Right now, it is just coded to handle Map and Collection objects. Others will be added.
 */
public class ObjectToString extends AbstractTransformer implements DiscoverableTransformer {

  private FeatureFlaggingService featureFlags;

  /** Give core transformers a slightly higher priority */
  private int priorityWeighting = DiscoverableTransformer.DEFAULT_PRIORITY_WEIGHTING + 1;

  public ObjectToString() {
    registerSourceType(DataType.OBJECT);
    registerSourceType(DataType.BYTE_ARRAY);
    registerSourceType(DataType.INPUT_STREAM);
    registerSourceType(DataType.CURSOR_STREAM_PROVIDER);
    registerSourceType(DataType.fromType(OutputHandler.class));
    setReturnDataType(DataType.STRING);
  }

  @Override
  public Object doTransform(Object src, Charset outputEncoding) throws TransformerException {
    String output;

    if (src instanceof CursorStreamProvider) {
      output = createStringFromInputStream(((CursorStreamProvider) src).openCursor(), outputEncoding);
    } else if (src instanceof CursorIteratorProvider) {
      // For some cases where repeatable streaming is disabled, this may consume the iterator making it unavailable for downstream
      // components in the flow.
      if (featureFlags.isEnabled(TO_STRING_TRANSFORMER_TRANSFORM_ITERATOR_ELEMENTS)) {
        output = createStringFromItearator(((CursorIteratorProvider) src).openCursor(), outputEncoding);
      } else {
        output = StringMessageUtils.toString(src);
      }
    } else if (src instanceof Iterator) {
      output = "<<Non repeatable iterator>>";
    } else if (src instanceof InputStream) {
      output = createStringFromInputStream((InputStream) src, outputEncoding);
    } else if (src instanceof OutputHandler) {
      output = createStringFromOutputHandler((OutputHandler) src, outputEncoding);
    } else if (src instanceof byte[]) {
      output = createStringFromByteArray((byte[]) src, outputEncoding);
    } else {
      output = StringMessageUtils.toString(src);
    }

    return output;
  }

  protected String createStringFromInputStream(InputStream input, Charset outputEncoding)
      throws TransformerException {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      copyLarge(input, outputStream);
      return outputStream.toString(outputEncoding.name());
    } catch (IOException e) {
      throw new TransformerException(errorReadingStream(), e);
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        logger.warn("Could not close stream", e);
      }
    }
  }

  protected String createStringFromItearator(CursorIterator input, Charset outputEncoding)
      throws TransformerException {
    try {
      StringBuilder output = new StringBuilder();

      output.append("[");
      for (Iterator iterator = input; iterator.hasNext();) {
        Object item = iterator.next();
        output.append(transform(item, outputEncoding).toString());
        output.append(", ");
      }
      output.delete(output.length() - 2, output.length());
      output.append("]");

      return output.toString();
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        logger.warn("Could not close stream", e);
      }
    }
  }

  protected String createStringFromOutputHandler(OutputHandler handler, Charset outputEncoding)
      throws TransformerException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      handler.write(getCurrentEvent(), bytes);
      return bytes.toString(outputEncoding.name());
    } catch (IOException e) {
      throw new TransformerException(this, e);
    }
  }

  protected String createStringFromByteArray(byte[] bytes, Charset outputEncoding)
      throws TransformerException {
    return new String(bytes, outputEncoding);
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
  public void setFeatureFlags(FeatureFlaggingService featureFlags) {
    this.featureFlags = featureFlags;
  }

  /**
   * Configures the {@link ZernovaRuntimeFeature#TO_STRING_TRANSFORMER_TRANSFORM_ITERATOR_ELEMENTS} feature flag.
   *
   * @since 4.4
   */
  public static void configureToStringTransformerTransformIteratorElements() {
    FeatureFlaggingRegistry featureFlaggingRegistry = FeatureFlaggingRegistry.getInstance();
    featureFlaggingRegistry.registerFeatureFlag(TO_STRING_TRANSFORMER_TRANSFORM_ITERATOR_ELEMENTS,
                                                featureContext -> featureContext.getArtifactMinZernovaVersion()
                                                    .filter(muleVersion -> muleVersion.atLeast(v4_4_0))
                                                    .isPresent());
  }
}
