/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.tx;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Allows to manipulate the transaction that was started and bound to an specific {@link SourceCallbackContext}
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface TransactionHandle extends com.zernova.sdk.api.tx.TransactionHandle {
}
