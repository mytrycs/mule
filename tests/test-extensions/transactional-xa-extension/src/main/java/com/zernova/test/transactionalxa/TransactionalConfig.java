/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactionalxa;

import com.zernova.sdk.api.annotation.Configuration;
import com.zernova.sdk.api.annotation.Operations;
import com.zernova.sdk.api.annotation.Sources;
import com.zernova.sdk.api.annotation.connectivity.ConnectionProviders;
import com.zernova.test.transactionalxa.connection.NonPoolingTransactionalConnectionProvider;
import com.zernova.test.transactionalxa.connection.PoolingTransactionalConnectionProvider;

@Configuration(name = "config")
@ConnectionProviders({PoolingTransactionalConnectionProvider.class, NonPoolingTransactionalConnectionProvider.class})
@Operations(TransactionalOperations.class)
@Sources({TransactionalSource.class, TransactionalSourceWithTXParameters.class})
public class TransactionalConfig {
}
