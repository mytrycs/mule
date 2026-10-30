/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;

/**
 * Contract interface for a {@link BaseDeclaration} in which it's possible to add/get {@link StereotypeModel} objects
 *
 * @since 1.0
 */
@NoImplement
public interface WithStereotypesDeclaration {

  /**
   * @return {@code this} components {@link StereotypeModel}
   */
  StereotypeModel getStereotype();

  /**
   * @param stereotype {@code this} components {@link StereotypeModel}
   */
  void withStereotype(StereotypeModel stereotype);

}
