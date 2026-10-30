/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.source;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.values.OfValues;
import com.zernova.test.values.extension.resolver.WithRequiredParametersValueProvider;

import java.util.List;

@MediaType(TEXT_PLAIN)
public class SourceWithValuesWithRequiredParameters extends AbstractSource {

  @OfValues(WithRequiredParametersValueProvider.class)
  @Parameter
  String channels;

  @Parameter
  String requiredString;

  @Parameter
  boolean requiredBoolean;

  @Parameter
  int requiredInteger;

  @Parameter
  List<String> strings;
}
