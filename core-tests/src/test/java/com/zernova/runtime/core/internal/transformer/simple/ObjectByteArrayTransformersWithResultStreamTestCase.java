/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import com.zernova.runtime.core.api.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ObjectByteArrayTransformersWithResultStreamTestCase extends ObjectByteArrayTransformersWithObjectsTestCase {

  @Override
  public Object getResultData() {
    byte[] resultData = (byte[]) super.getResultData();
    return new ByteArrayInputStream(resultData);
  }

  @Override
  public boolean compareResults(Object expected, Object result) {
    if (expected instanceof InputStream) {
      InputStream input = (InputStream) expected;
      byte[] bytes = IOUtils.toByteArray(input);
      return super.compareResults(bytes, result);
    }
    return super.compareResults(expected, result);
  }

}
