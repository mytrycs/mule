/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.metadata;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_XML;
import static com.zernova.runtime.api.metadata.MediaType.parse;
import static com.zernova.runtime.api.metadata.MediaTypeUtils.getStringRepresentableMimeTypes;
import static com.zernova.runtime.api.metadata.MediaTypeUtils.isStringRepresentable;

import com.zernova.runtime.api.metadata.MediaType;

import org.junit.Test;

public class MediaTypeUtilsTestCase {

  @Test
  public void anyTextMimeTypeIsStringRepresentable() {
    assertThat(isStringRepresentable(parse("text/lalalalalalala")), is(true));
  }

  @Test
  public void applicationJsonIsStringRepresentable() {
    assertThat(isStringRepresentable(MediaType.JSON), is(true));
  }

  @Test
  public void binaryTypeIsNotStringRepresentable() {
    assertThat(isStringRepresentable(parse("application/octet-stream")), is(false));
  }

  @Test
  public void applicationXmlIsFoundInStringRepresentableMimeTypes() {
    assertThat(getStringRepresentableMimeTypes(), hasItem(APPLICATION_XML));
  }

  @Test(expected = NullPointerException.class)
  public void mediaTypeIsRequired() {
    isStringRepresentable(null);
  }
}
