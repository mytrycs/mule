/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.util.MultiMap;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.internal.transformer.AbstractDiscoverableTransformer;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Converts a {@link Map} to a {@link MultiMap}.
 *
 * @since 4.0
 */
public class MapToMultiMap extends AbstractDiscoverableTransformer {

  public MapToMultiMap() {
    registerSourceType(DataType.fromType(Map.class));
    setReturnDataType(DataType.fromType(MultiMap.class));
  }

  @Override
  protected Object doTransform(Object src, Charset enc) throws TransformerException {
    Map map = (Map) src;
    MultiMap multiMap = new MultiMap(map);
    return multiMap;
  }
}
