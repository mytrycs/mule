/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.datatype;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.DataTypeBuilder;
import com.zernova.runtime.core.api.transformer.DiscoverableTransformer;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;

import java.nio.charset.Charset;

/**
 * Converts strings to {@link Charset} instances. See {@link DataTypeBuilder#charset(String)}
 */
public class StringToCharsetTransformer extends AbstractTransformer implements DiscoverableTransformer {

  private int priorityWeighting = DiscoverableTransformer.DEFAULT_PRIORITY_WEIGHTING;

  public StringToCharsetTransformer() {
    this.registerSourceType(DataType.STRING);
    this.setReturnDataType(DataType.builder().type(Charset.class).build());
  }

  @Override
  protected Object doTransform(Object src, Charset enc) throws TransformerException {
    try {
      return DataType.builder().charset((String) src).build().getMediaType().getCharset().get();
    } catch (Exception e) {
      throw new TransformerException(createStaticMessage("Exception transforming to Charset."), e);
    }
  }

  @Override
  public int getPriorityWeighting() {
    return priorityWeighting;
  }

  @Override
  public void setPriorityWeighting(int priorityWeighting) {
    this.priorityWeighting = priorityWeighting;
  }

}
