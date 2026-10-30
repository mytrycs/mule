/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.probe;

import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.core.api.util.func.CheckedSupplier;

/**
 * Implementation of {@link JUnitProbe} which makes it Java 8 Lambda friendly.
 */
public class JUnitLambdaProbe extends JUnitProbe {

  private final CheckedSupplier<Boolean> probable;
  private final CheckedFunction<AssertionError, String> failureDescription;

  public JUnitLambdaProbe(CheckedSupplier<Boolean> probable, String failureDescription) {
    this.probable = probable;
    this.failureDescription = ae -> failureDescription;
  }

  public JUnitLambdaProbe(CheckedSupplier<Boolean> probable, CheckedFunction<AssertionError, String> failureDescription) {
    this.probable = probable;
    this.failureDescription = failureDescription;
  }

  public JUnitLambdaProbe(CheckedSupplier<Boolean> probable, CheckedSupplier<String> failureDescription) {
    this.probable = probable;
    this.failureDescription = ae -> failureDescription.get();
  }

  public JUnitLambdaProbe(CheckedSupplier<Boolean> probable) {
    this(probable, ae -> null);
  }

  @Override
  public String describeFailure() {
    String description = failureDescription.apply(lastFailure);
    return description == null ? super.describeFailure() : description;
  }

  @Override
  protected boolean test() throws Exception {
    return probable.get();
  }
}
