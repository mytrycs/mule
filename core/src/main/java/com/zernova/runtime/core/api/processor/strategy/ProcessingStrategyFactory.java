/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.processor.strategy;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.core.api.ZernovaContext;

/**
 * Abstract Factory for creating concrete {@link ProcessingStrategy}ies.
 *
 * @since 4.0
 */
@NoImplement
public interface ProcessingStrategyFactory {

  /**
   * @return a new {@link ProcessingStrategy}.
   */
  ProcessingStrategy create(ZernovaContext muleContext, String schedulersNamePrefix);

  /**
   * Provides a hint to users of the factory about the concrete type of {@link ProcessingStrategy} to be built.
   *
   * @return a reference to the concrete implementation of the {@link ProcessingStrategy} this factory will create.
   */
  default Class<? extends ProcessingStrategy> getProcessingStrategyType() {
    return ProcessingStrategy.class;
  }

}
