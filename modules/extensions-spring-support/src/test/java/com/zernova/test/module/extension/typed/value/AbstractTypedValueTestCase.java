/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.typed.value;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.zernova.tck.junit4.matcher.DataTypeMatcher.like;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.nio.charset.Charset;

public abstract class AbstractTypedValueTestCase extends AbstractExtensionFunctionalTestCase {

  static final Charset UTF8 = Charset.forName("UTF-8");
  static final MediaType APPLICATION_JAVA = MediaType.parse("application/java");
  static final MediaType WILDCARD = MediaType.parse("*/*");
  static final KnockeableDoor DOOR = new KnockeableDoor("Saul");

  @Override
  protected boolean isDisposeContextPerClass() {
    return true;
  }

  void runAndAssertTypedValue(String flowName, Object payloadValue, MediaType mediaType, Charset charset)
      throws Exception {
    Object payload = flowRunner(flowName).run().getMessage().getPayload().getValue();
    assertTypedValue((TypedValue) payload, payloadValue, mediaType, charset);
  }

  void assertTypedValue(TypedValue typedValue, Object payloadValue, MediaType mediaType, Charset charset) {
    assertThat(typedValue, is(instanceOf(TypedValue.class)));
    Object value = typedValue.getValue();
    assertThat(value, is(payloadValue));
    if (value != null) {
      assertThat(value, is(instanceOf(payloadValue.getClass())));
      assertThat(typedValue.getDataType(), is(like(payloadValue.getClass(), mediaType, charset)));
    }
  }
}
