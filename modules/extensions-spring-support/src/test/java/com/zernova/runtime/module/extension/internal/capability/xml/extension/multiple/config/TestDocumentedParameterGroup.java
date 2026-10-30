/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.extension.multiple.config;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

public class TestDocumentedParameterGroup {

  /**
   * Group parameter 1
   */
  @Parameter
  private String value1;

  /**
   * Group parameter 2
   */
  @Parameter
  private String value2;

  /**
   * Param with alias
   */
  @Parameter
  @Alias("alias-param")
  private String aliasedParam;
}
