/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.weave.v2.el.ByteArrayBasedCursorStreamProvider;

import org.junit.Test;

public class ExpressionEvaluatorTestCase extends AbstractZernovaContextTestCase {

  @Test
  public void handleNullEvent() throws ZernovaException {
    TypedValue evaluate = muleContext.getExpressionManager().evaluate("%dw 2.0\noutput application/json\n---\n{a: 1}");
    ByteArrayBasedCursorStreamProvider value = (ByteArrayBasedCursorStreamProvider) evaluate.getValue();
    String expected = "{\n" +
        "  \"a\": 1\n" +
        "}";
    assertThat(IOUtils.toString(value), is(expected));
  }
}
