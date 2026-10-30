/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static java.util.stream.Collectors.joining;
import com.zernova.runtime.extension.api.annotation.param.ExclusiveOptionals;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.stream.Stream;

@ExclusiveOptionals
public class VeganArguments {

  @Parameter
  @com.zernova.sdk.api.annotation.param.Optional
  private String argument1;

  @Parameter
  @Optional
  private String argument2;

  @Parameter
  @Optional
  private String argument3;

  public boolean hasAtLeastOneArgument() {
    return argument1 != null || argument2 != null || argument3 != null;
  }

  public String getArgument1() {
    return argument1;
  }

  public String getArgument2() {
    return argument2;
  }

  public String getArgument3() {
    return argument3;
  }

  @Override
  public String toString() {
    return Stream.of(argument1, argument2, argument3)
        .filter(a -> a != null)
        .collect(joining(","));
  }
}
