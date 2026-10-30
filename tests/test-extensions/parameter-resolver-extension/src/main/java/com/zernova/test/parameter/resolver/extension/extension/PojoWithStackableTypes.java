/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.parameter.resolver.extension.extension;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.sdk.api.runtime.parameter.Literal;

import java.util.Objects;

public class PojoWithStackableTypes {

  @Parameter
  @Optional
  Literal<String> literalString;

  @Parameter
  @Optional
  ParameterResolver<String> parameterResolverString;

  @Parameter
  @Optional
  TypedValue<String> typedValueString;

  public Literal<String> getLiteralString() {
    return literalString;
  }

  public void setLiteralString(Literal<String> literalString) {
    this.literalString = literalString;
  }

  public ParameterResolver<String> getParameterResolverString() {
    return parameterResolverString;
  }

  public void setParameterResolverString(ParameterResolver<String> parameterResolverString) {
    this.parameterResolverString = parameterResolverString;
  }

  public TypedValue<String> getTypedValueString() {
    return typedValueString;
  }

  public void setTypedValueString(TypedValue<String> typedValueString) {
    this.typedValueString = typedValueString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    PojoWithStackableTypes that = (PojoWithStackableTypes) o;
    return Objects.equals(literalString, that.literalString) &&
        Objects.equals(parameterResolverString, that.parameterResolverString) &&
        Objects.equals(typedValueString, that.typedValueString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(literalString, parameterResolverString, typedValueString);
  }
}
