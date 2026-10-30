/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.subtypes.extension;

import com.zernova.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.heisenberg.extension.model.Weapon;

@TypeDsl(allowTopLevelDefinition = true)
public class Revolver implements Weapon {

  @Parameter
  @Optional(defaultValue = "6")
  private int bullets;

  public int getBullets() {
    return bullets;
  }

  @Override
  public String kill() {
    return bullets > 0 ? "BANG" : "LUCKY";
  }
}
