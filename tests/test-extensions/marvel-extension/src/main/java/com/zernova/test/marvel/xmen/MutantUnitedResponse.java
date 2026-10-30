/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.xmen;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.Summary;

public class MutantUnitedResponse {


  @Parameter
  @ParameterDsl(allowReferences = false)
  @Content(primary = true)
  @Summary("The body of the Message")
  private TypedValue<Object> body;

  public TypedValue<Object> getBody() {
    return body;
  }

}
