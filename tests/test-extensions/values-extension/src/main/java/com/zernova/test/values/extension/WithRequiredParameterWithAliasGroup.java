/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.values.OfValues;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.values.extension.resolver.WithRequiredParameterWithAliasValueProvider;

public class WithRequiredParameterWithAliasGroup {

  @Parameter
  @Alias("superString")
  String requiredString;

  @Parameter
  @OfValues(WithRequiredParameterWithAliasValueProvider.class)
  String channels;
}
