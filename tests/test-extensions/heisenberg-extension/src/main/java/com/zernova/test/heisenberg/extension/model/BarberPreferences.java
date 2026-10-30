/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.semantics.SemanticTerms;

public class BarberPreferences {

  public enum BEARD_KIND {
    GOATIE, MUSTACHE
  }

  @Parameter
  @Optional
  @SemanticTerms("hairy")
  private boolean fullyBald;

  @Parameter
  @Optional(defaultValue = "GOATIE")
  private BEARD_KIND beardTrimming;

  public boolean isFullyBald() {
    return fullyBald;
  }

  public BEARD_KIND getBeardTrimming() {
    return beardTrimming;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof BarberPreferences)) {
      return false;
    }

    BarberPreferences that = (BarberPreferences) o;
    return fullyBald == that.fullyBald && beardTrimming == that.beardTrimming;
  }

  @Override
  public int hashCode() {
    return 31 * (fullyBald ? 1 : 0) + beardTrimming.hashCode();
  }

}
