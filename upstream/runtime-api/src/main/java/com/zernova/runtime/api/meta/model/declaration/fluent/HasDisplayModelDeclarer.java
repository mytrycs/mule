/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;


import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.display.DisplayModel;

/**
 * Contract interface for a declarer in which it's possible to add a {@link DisplayModel}
 *
 * @since 1.0
 */
@NoImplement
public interface HasDisplayModelDeclarer<T> {

  /**
   * Sets the given {@code displayModel}
   *
   * @param displayModel a {@link DisplayModel}
   * @return {@code this} declarer
   */
  T withDisplayModel(DisplayModel displayModel);
}
