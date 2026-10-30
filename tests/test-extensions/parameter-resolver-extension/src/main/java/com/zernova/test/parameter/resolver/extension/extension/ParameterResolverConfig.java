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
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.parameter.Literal;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;

import java.io.InputStream;

@Configuration(name = "config")
@Operations(ParameterResolverParameterOperations.class)
@Sources(SomeSource.class)
public class ParameterResolverConfig extends ParameterResolverExtension {

  @Parameter
  @Optional
  private ParameterResolver<String> stringResolver;

  @Parameter
  @Optional
  private com.zernova.sdk.api.runtime.parameter.ParameterResolver<KnockeableDoor> doorResolver;

  @Parameter
  @Optional
  private Literal<KnockeableDoor> literalDoor;

  @Parameter
  @Optional
  private ParameterResolver<TypedValue<InputStream>> lazyParameter;

  @Parameter
  @Optional
  private com.zernova.sdk.api.runtime.parameter.ParameterResolver<ParameterResolver<ParameterResolver<com.zernova.sdk.api.runtime.parameter.ParameterResolver<TypedValue<InputStream>>>>> nestedParameter;

  @Parameter
  @Optional
  private ParameterResolver<Literal<String>> resolverOfLiteral;

  public ParameterResolver<String> getStringResolver() {
    return stringResolver;
  }

  public void setStringResolver(ParameterResolver<String> stringResolver) {
    this.stringResolver = stringResolver;
  }

  public com.zernova.sdk.api.runtime.parameter.ParameterResolver<KnockeableDoor> getDoorResolver() {
    return doorResolver;
  }

  public void setDoorResolver(ParameterResolver<KnockeableDoor> doorResolver) {
    this.doorResolver = doorResolver;
  }

  public Literal<KnockeableDoor> getLiteralDoor() {
    return literalDoor;
  }

  public void setLiteralDoor(Literal<KnockeableDoor> literalDoor) {
    this.literalDoor = literalDoor;
  }
}
