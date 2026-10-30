/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static com.zernova.test.petstore.extension.PetStoreOperationsWithFailures.CONNECTION_FAIL;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;

@Alias("invalid")
public class PooledPetStoreConnectionProviderWithFailureInvalidConnection
    extends PetStoreConnectionProvider<PetStoreClient> implements ConnectionProvider<PetStoreClient> {

  @Override
  public ConnectionValidationResult validate(PetStoreClient connection) {
    return ConnectionValidationResult.failure(CONNECTION_FAIL, new Exception("Invalid credentials"));
  }
}
