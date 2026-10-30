/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy;

import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;

/**
 * Marker interface for {@link ProcessingStrategyFactory}'s that create {@link ProcessingStrategy}'s that are transaction aware
 * and will not fail when a transaction is active. Typically this will be supported by forcing single-threaded behaviour when a
 * transaction is present.
 *
 * @since 4.0
 */
public interface TransactionAwareProcessingStrategyFactory extends ProcessingStrategyFactory {

}
