/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.result;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class PayloadTargetReturnDelegateTestCase extends TargetReturnDelegateTestCase {

  @Override
  protected ReturnDelegate createReturnDelegate() {
    return new PayloadTargetReturnDelegate(TARGET, componentModel, artifactEncoding);
  }

  @Override
  protected Message getOutputMessage(CoreEvent result) {
    TypedValue<?> typedValue = result.getVariables().get(TARGET);
    return Message.builder().payload(typedValue).mediaType(typedValue.getDataType().getMediaType()).build();
  }

  @Override
  @Test
  public void operationReturnsOperationResultThatOnlySpecifiesPayloadAndAttributes() throws Exception {
    // this tests doesn't apply
  }
}
