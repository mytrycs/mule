/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.connectivity;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Callback used to notify the runtime about the outcome of a reconnection attempt.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface ReconnectionCallback extends com.zernova.sdk.api.runtime.connectivity.ReconnectionCallback {

}
