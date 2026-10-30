/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.parser;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;

import java.util.List;

/**
 * General contract for a model parser capable of reading a component's allowed stereotypes
 *
 * @since 1.10.0
 */
@NoImplement
public interface AllowedStereotypesModelParser {

  /**
   * Returns a {@link List} of allowed stereotypes. Each item is created through the provided {@code factory}.
   *
   * @param factory a {@link StereotypeModelFactory}
   * @return a list. Might be empty but will never be {@code null}
   */
  List<StereotypeModel> getAllowedStereotypes(StereotypeModelFactory factory);
}
