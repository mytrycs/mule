/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.connectivity;

import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Connections which support transaction must implement this interface in order to start or join a current transaction.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public interface TransactionalConnection extends com.zernova.sdk.api.connectivity.TransactionalConnection {

}
