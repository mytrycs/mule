/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.xmen;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.List;

public class CanadianForest {

  @Parameter
  @Content(primary = true)
  private List<String> bears;

  @Parameter
  @Content
  private TypedValue<Object> river;

  @Parameter
  private List<String> friends;

  public CanadianForest() {
    super();
  }

  public List<String> getBears() {
    return bears;
  }

  public TypedValue<Object> getRiver() {
    return river;
  }

  public List<String> getFriends() {
    return friends;
  }
}
