/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.implicit.exclusive.config.extension.extension;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

@Operations({OperationWithConfigOverride.class})
@Configuration(name = "bla-with-default")
public class ImplicitConfigWithOptionalParameter {

  public static final String OPTIONAL_PARAMETER_DEFAULT_VALUE = "Default Value";

  @Parameter
  @Optional(defaultValue = OPTIONAL_PARAMETER_DEFAULT_VALUE)
  private String optionalWithStaticDefault;

}
