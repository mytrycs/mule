/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.subtypes.extension;

import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.Objects;

public class NoReferencePojo {

  @Parameter
  private int number;

  @Parameter
  private String string;

  public int getNumber() {
    return number;
  }

  public String getString() {
    return string;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    NoReferencePojo that = (NoReferencePojo) o;
    return number == that.number &&
        Objects.equals(string, that.string);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number, string);
  }
}
