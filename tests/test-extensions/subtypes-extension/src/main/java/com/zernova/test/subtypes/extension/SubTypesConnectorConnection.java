/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.subtypes.extension;

public class SubTypesConnectorConnection {

  private ParentShape shape;
  private Door door;


  public SubTypesConnectorConnection(ParentShape shape, Door door) {
    this.shape = shape;
    this.door = door;
  }

  public ParentShape getShape() {
    return shape;
  }

  public Door getDoor() {
    return door;
  }
}
