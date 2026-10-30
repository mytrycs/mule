/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.model.attribute;

import com.zernova.runtime.extension.api.annotation.dsl.xml.TypeDsl;

import java.io.Serializable;

@TypeDsl(allowTopLevelDefinition = true)
public class AnimalsOutputAttributes implements Serializable, AbstractOutputAttributes {

  private String outputId = "AnimalsOutputAttributes";
  private String kind;

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  @Override
  public String getOutputId() {
    return outputId;
  }
}
