/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.parser;

import com.zernova.api.annotation.NoImplement;

import java.util.Set;

/**
 * General contract for a model parser capable of reading semantic terms
 *
 * @since 1.10.0
 */
@NoImplement
public interface SemanticTermsParser {

  /**
   * @return a {@link Set} with the model's semantic terms
   */
  Set<String> getSemanticTerms();
}
