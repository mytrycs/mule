/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.model.response;

import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.metadata.extension.resolver.TestMetadataInputHouseResolver;

import java.io.InputStream;

public class ErrorResponse {

  @Parameter
  @TypeResolver(TestMetadataInputHouseResolver.class)
  private InputStream response;

  @Parameter
  private Integer code;

  public InputStream getResponse() {
    return response;
  }

  public Integer getCode() {
    return code;
  }
}
