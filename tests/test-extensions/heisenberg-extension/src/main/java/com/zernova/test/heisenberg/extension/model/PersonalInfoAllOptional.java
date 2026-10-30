/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.AGE;

import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.Placement;

import java.util.List;

public class PersonalInfoAllOptional {

  @Parameter
  @Optional(defaultValue = AGE)
  @Placement(order = 1)
  private Integer age;

  @Parameter
  @Optional
  @Placement(order = 2)
  private List<String> knownAddresses;

  public List<String> getKnownAddresses() {
    return knownAddresses;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  @Override
  public boolean equals(Object obj) {
    return reflectionEquals(obj, this);
  }
}
