/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.parser.metadata;

import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.resolving.PartialTypeKeysResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParser;

/**
 * Parses the syntactic definition of the metadata keys id so that the semantics reflected in it can be extracted in a uniform
 * way, regardless of the actual syntax used by the extension developer.
 *
 * @see ExtensionModelParser
 * @since 1.10.0
 */
@NoImplement
public interface MetadataKeyModelParser {

  /**
   * @return {@code true} if this parser parse a Key resolver different from {@link NullMetadataResolver} and
   *         {@link com.zernova.runtime.extension.api.metadata.NullMetadataResolver}
   */
  boolean hasKeyIdResolver();

  /**
   * @return {@code true} if this parser parse a Key resolver is an instance of {@link PartialTypeKeysResolver} or
   *         {@link com.zernova.runtime.api.metadata.resolving.PartialTypeKeysResolver}
   */
  boolean isPartialKeyResolver();

  /**
   * @return an instance of {@link TypeKeysResolver}
   */
  TypeKeysResolver getKeyResolver();

  /**
   * @return an instance the {@link MetadataType}
   */
  MetadataType getMetadataType();

  /**
   * @return the parameter name of the metadata key
   */
  String getParameterName();
}
