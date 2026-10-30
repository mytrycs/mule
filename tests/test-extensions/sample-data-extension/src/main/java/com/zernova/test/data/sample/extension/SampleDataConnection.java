/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension;

import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JSON;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_XML;
import static com.zernova.test.data.sample.extension.SampleDataExtension.NULL_VALUE;

import com.zernova.runtime.extension.api.runtime.operation.Result;


public class SampleDataConnection {

  public Result<String, String> getResult(String payload, String attributes) {
    return Result.<String, String>builder()
        .output(payload)
        .mediaType(APPLICATION_JSON)
        .attributes(attributes != null ? attributes : NULL_VALUE)
        .attributesMediaType(APPLICATION_XML)
        .build();
  }
}
