/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.drstrange;

import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.Placement;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;
import com.zernova.runtime.extension.api.stereotype.ObjectStoreStereotype;

@Stereotype(DrStrangeStereotypeDefinition.class)
public class DrStrangeTypeWithCustomStereotype {

  @Parameter
  private String storeName;
}
