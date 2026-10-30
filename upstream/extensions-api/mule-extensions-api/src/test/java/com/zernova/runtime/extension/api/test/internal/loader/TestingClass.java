/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.internal.loader;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.sdk.api.runtime.parameter.Literal;
import com.zernova.sdk.api.runtime.parameter.ParameterResolver;

public final class TestingClass {

  private ParameterResolver<TestingClass> paramResolverRecursive;
  private TypedValue<TestingClass> typedValueRecursive;
  private Literal<TestingClass> literalRecursive;

}
