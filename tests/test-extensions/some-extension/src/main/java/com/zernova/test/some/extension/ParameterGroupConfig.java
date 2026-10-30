/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.some.extension;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;

import java.time.ZonedDateTime;

@Configuration(name = "config")
@Operations({SomeOps.class})
public class ParameterGroupConfig {

  @Parameter
  @Optional
  private ZonedDateTime zonedDateTime;

  @ParameterGroup(name = "parameter-group")
  SomeParameterGroupOneRequiredConfig someParameterGroup;

  public SomeParameterGroupOneRequiredConfig getSomeParameterGroup() {
    return someParameterGroup;
  }

  public ZonedDateTime getZonedDateTime() {
    return zonedDateTime;
  }
}
