/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.message;

import static com.zernova.runtime.api.metadata.DataType.JSON_STRING;
import static com.zernova.runtime.api.metadata.MediaType.TEXT;
import static com.zernova.tck.junit4.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;

public class DefaultZernovaMessageTestCase extends AbstractZernovaContextTestCase {

  @Test
  public void regularToString() {
    Message message = Message.builder()
        .payload(TypedValue.of("test"))
        .attributes(new TypedValue<>("{}", JSON_STRING))
        .mediaType(TEXT)
        .build();

    assertThat(message.toString(), is(equalToIgnoringLineBreaks("\n" +
        "com.zernova.runtime.core.internal.message.DefaultMessageBuilder$MessageImplementation\n"
        + "{\n"
        + "  payload=test\n"
        + "  mediaType=text/plain\n"
        + "  attributes={}\n"
        + "  attributesMediaType=application/json\n"
        + "}")));
  }

}
