/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.streaming;

import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * This interface provides functionality for consuming a data feed in pages.
 * <p>
 * Instances should not be reused. Each execution of a paging operation should return different instances.
 * <p>
 * Implementing this interface does not guarantee thread safeness.
 *
 * @param <C> connection type expected to handle the operations.
 * @param <T> the type of the returned pages.
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public interface PagingProvider<C, T> extends com.zernova.sdk.api.runtime.streaming.PagingProvider<C, T> {

}
