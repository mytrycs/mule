/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.parameter.Literal;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.util.Objects;

@TypeDsl(allowTopLevelDefinition = true)
public class DifferedKnockableDoor {

  @Parameter
  @Optional
  private ParameterResolver<String> victim;

  @Parameter
  @Optional
  private TypedValue<String> address;

  @Parameter
  @Optional
  private com.zernova.sdk.api.runtime.parameter.Literal<String> encodedPasssword;

  public ParameterResolver<String> getVictim() {
    return victim;
  }

  public TypedValue<String> getAddress() {
    return address;
  }

  public com.zernova.sdk.api.runtime.parameter.Literal<String> getEncodedPasssword() {
    return encodedPasssword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DifferedKnockableDoor that = (DifferedKnockableDoor) o;
    return Objects.equals(victim, that.victim) &&
        Objects.equals(address, that.address) &&
        Objects.equals(encodedPasssword, that.encodedPasssword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(victim, address, encodedPasssword);
  }
}
