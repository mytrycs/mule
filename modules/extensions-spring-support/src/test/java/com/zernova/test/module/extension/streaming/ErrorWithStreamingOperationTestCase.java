/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.streaming;

import static org.hamcrest.Matchers.containsString;

import com.zernova.functional.api.exception.ExpectedError;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Rule;
import org.junit.Test;

public class ErrorWithStreamingOperationTestCase extends AbstractExtensionFunctionalTestCase {

  private final long AMOUNT_OF_CALLS = 10;
  private final String SIGNATURE = "This is my signature";

  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return "streaming/error-with-streaming-operation-config.xml";
  }

  @Test
  public void connectionIsReleasedWhenOperationFails() throws Exception {
    expectedError.expectErrorType("MULE", "UNKNOWN");
    expectedError.expectMessage(containsString("The operation failed!"));

    for (int i = 0; i < AMOUNT_OF_CALLS; i++) {
      try {
        flowRunner("streaming-operation-with-error").withVariable("signature", SIGNATURE).run();
      } catch (Exception e) {
        // Do Nothing
      }
    }

    flowRunner("streaming-operation-with-error").withVariable("signature", SIGNATURE).run();
  }

}
