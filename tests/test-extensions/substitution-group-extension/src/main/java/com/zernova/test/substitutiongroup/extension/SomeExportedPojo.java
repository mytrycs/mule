/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.substitutiongroup.extension;

import com.zernova.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

@TypeDsl(substitutionGroup = "heisenberg:global-abstract-weapon",
    baseType = "heisenberg:com.zernova.test.heisenberg.extension.model.Weapon", allowTopLevelDefinition = true)
public class SomeExportedPojo {

  @Parameter
  private boolean attribute;

}
