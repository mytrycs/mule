/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.values.ValuePart;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.DisplayName;

public class GroupAsMultiLevelValue {

  @Parameter
  @Optional(defaultValue = "some value")
  @Alias("required")
  private String wsdlLocation;

  @Optional(defaultValue = "AMERICA")
  @ValuePart(order = 1)
  @Parameter
  private String continent;

  @Optional(defaultValue = "USA")
  @com.zernova.sdk.api.annotation.values.ValuePart(order = 2)
  @Parameter
  private String country;

  @Optional(defaultValue = "SFO")
  @DisplayName("State | City")
  @ValuePart(order = 3)
  @Parameter
  private String city;

}
