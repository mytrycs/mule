/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.parameter.resolver.extension.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.test.heisenberg.extension.model.DifferedKnockableDoor;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;

import java.io.InputStream;

public class ParameterResolverParameterOperations {

  public ParameterResolverExtension configOperation(@Config ParameterResolverExtension config) {
    return config;
  }

  public DifferedKnockableDoor resolverOperation(DifferedKnockableDoor differedDoor) {
    return differedDoor;
  }

  public ParameterResolver<KnockeableDoor> doorOperation(@Optional ParameterResolver<KnockeableDoor> door,
                                                         @Optional KnockeableDoor someDoor) {
    return door;
  }

  @MediaType(TEXT_PLAIN)
  public ParameterResolver<String> stringOperation(@Optional(
      defaultValue = "this is a string") ParameterResolver<String> string) {
    return string;
  }

  @MediaType(TEXT_PLAIN)
  public ParameterResolver<TypedValue<InputStream>> lazyValueOperation(@Optional ParameterResolver<TypedValue<InputStream>> lazyValue) {
    return lazyValue;
  }

  public PojoWithStackableTypes pojoWithStackableTypes(@Optional PojoWithStackableTypes stackableTypes) {
    return stackableTypes;
  }

}
