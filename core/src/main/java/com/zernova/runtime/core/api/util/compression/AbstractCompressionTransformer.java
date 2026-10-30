/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.util.compression;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;

/**
 * <code>AbstractCompressionTransformer</code> is a base class for all transformers that can compress or uncompress data when they
 * performa message transformation. Compression is done via a pluggable strategy.
 *
 * @deprecated No implementations remain of this class.
 */
@NoExtend
@Deprecated
public abstract class AbstractCompressionTransformer extends AbstractTransformer {

  private CompressionStrategy strategy;

  /**
   * default constructor required for discovery
   */
  public AbstractCompressionTransformer() {
    super();
  }

  public CompressionStrategy getStrategy() {
    return strategy;
  }

  public void setStrategy(CompressionStrategy strategy) {
    this.strategy = strategy;
  }

}
