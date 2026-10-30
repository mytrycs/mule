/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.simple;

import static java.nio.charset.Charset.defaultCharset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class StringToBooleanTestCase extends AbstractZernovaTestCase {

  private StringToBoolean transformer;

  @Before
  public void setUp() {
    transformer = new StringToBoolean();
    transformer.setArtifactEncoding(() -> defaultCharset());
  }

  @Test
  public void validTransforms() {
    assertBoolean(true, "true", "TRUE", "trUE ", "yes", "1");
    assertBoolean(false, "false", "FALSE", "no ", "NO ", "0");
  }

  @Test(expected = TransformerException.class)
  public void invalidValue() throws Exception {
    transformer.transform("cuchurrumin");
  }

  private void assertBoolean(boolean expected, String... values) {
    Stream.of(values).forEach(value -> {
      try {
        assertThat(transformer.transform(value), is(expected));
      } catch (TransformerException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
