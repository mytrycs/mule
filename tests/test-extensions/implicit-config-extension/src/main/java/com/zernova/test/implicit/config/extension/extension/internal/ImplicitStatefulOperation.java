/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.implicit.config.extension.extension.internal;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.implicit.config.extension.extension.api.ImplicitConfigExtension;

public class ImplicitStatefulOperation {

  @Parameter
  @ConfigOverride
  private String optionalWithDefault;

  @MediaType("text/plain")
  public String getEnrichedName(@Config ImplicitConfigExtension config) {
    return config.getName() + " " + optionalWithDefault;
  }

}
