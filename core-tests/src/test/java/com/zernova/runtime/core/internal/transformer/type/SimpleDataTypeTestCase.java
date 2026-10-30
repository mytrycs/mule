/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.type;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static com.zernova.runtime.api.metadata.MediaType.JSON;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.nio.charset.UnsupportedCharsetException;

import org.junit.Test;

@SmallTest
public class SimpleDataTypeTestCase extends AbstractZernovaTestCase {

  @Test
  public void acceptsSupportedEncoding() throws Exception {
    DataType dataType = DataType.builder().charset(UTF_8.name()).build();

    assertThat(dataType.getMediaType().getCharset().get(), equalTo(UTF_8));
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void rejectsUnsupportedEncoding() throws Exception {
    DataType.builder().charset("unsupportedEncoding").build();
  }

  @Test
  public void acceptsValidMimeType() throws Exception {
    DataType dataType = DataType.builder().mediaType(JSON).build();

    assertThat(dataType.getMediaType(), equalTo(JSON));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsValidMimeType() throws Exception {
    DataType.builder().mediaType("invalidMimeType").build();
  }
}
