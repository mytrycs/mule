/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.parameter.resolver.extension.extension;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.parameter.Literal;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;

import java.io.InputStream;

@Configuration(name = "nested-types-config")
@Operations(ParameterResolverParameterOperations.class)
public class NestedWrapperTypesConfig extends ParameterResolverExtension {

  @Parameter
  @Optional
  private ParameterResolver<TypedValue<KnockeableDoor>> doorResolver;

  @Parameter
  @Optional
  private ParameterResolver<TypedValue<InputStream>> lazyParameter;

  @Parameter
  @Optional
  private ParameterResolver<ParameterResolver<com.zernova.sdk.api.runtime.parameter.ParameterResolver<ParameterResolver<TypedValue<InputStream>>>>> nestedParameter;

  @Parameter
  @Optional
  private ParameterResolver<com.zernova.sdk.api.runtime.parameter.Literal<String>> resolverOfLiteral;

  @Parameter
  @Optional
  private ParameterResolver<TypedValue<String>> lazyString;

  public ParameterResolver<TypedValue<KnockeableDoor>> getDoorResolver() {
    return doorResolver;
  }

  public ParameterResolver<TypedValue<InputStream>> getLazyParameter() {
    return lazyParameter;
  }

  public ParameterResolver<ParameterResolver<com.zernova.sdk.api.runtime.parameter.ParameterResolver<ParameterResolver<TypedValue<InputStream>>>>> getNestedParameter() {
    return nestedParameter;
  }

  public ParameterResolver<com.zernova.sdk.api.runtime.parameter.Literal<String>> getResolverOfLiteral() {
    return resolverOfLiteral;
  }

  public ParameterResolver<TypedValue<String>> getLazyString() {
    return lazyString;
  }
}
