/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.substitutiongroup.extension;

import com.zernova.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.Objects;

@TypeDsl(substitutionGroup = "mule:abstract-shared-extension")
public class ZernovaSGPojo {

  @Parameter
  boolean innerParameter;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ZernovaSGPojo that = (ZernovaSGPojo) o;
    return innerParameter == that.innerParameter;
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerParameter);
  }
}
