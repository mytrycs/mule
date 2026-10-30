/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.subtypes.extension;

import com.zernova.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.Optional;

@TypeDsl(allowTopLevelDefinition = true)
public class HouseDoor implements Door {

  @Parameter
  private boolean locked;

  @Parameter
  @Optional(defaultValue = "pivotal")
  private String handle;

  @Override
  public void open() {}

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  @Override
  public String getHandle() {
    return this.handle;
  }
}
