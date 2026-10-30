/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.parser.metadata;

import com.zernova.api.annotation.NoImplement;
import com.zernova.sdk.api.metadata.resolving.ChainInputTypeResolver;


/**
 * Parses the syntactic definition of a scope to obtain its inner chain's {@link ChainInputTypeResolver}
 *
 * @since 1.10.0
 */
@NoImplement
public interface ScopeChainInputTypeResolverModelParser {

  /**
   * Parses the {@link ChainInputTypeResolver} for the scope's inner chain. If none is specified, a default instance implementing
   * the {@code Null-Object} design pattern will be returned.
   *
   * @return a non {@code null} {@link ChainInputTypeResolver}
   */
  ChainInputTypeResolver getChainInputTypeResolver();
}
