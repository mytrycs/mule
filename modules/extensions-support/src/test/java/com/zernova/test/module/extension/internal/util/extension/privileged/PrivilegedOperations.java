/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.privileged;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;

public class PrivilegedOperations {

  @MediaType(TEXT_PLAIN)
  public String doSomething() {
    return "I did something";
  }

  @MediaType(TEXT_PLAIN)
  public void doSomethingAsync(CompletionCallback<String, Void> completionCallback) {

  }
}
