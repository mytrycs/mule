/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.PoolingConnectionProvider;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;
import com.zernova.test.petstore.extension.stereotype.CustomPetstoreConnectionStereotype;

@Alias("pooled")
@Stereotype(CustomPetstoreConnectionStereotype.class)
public class PooledPetStoreConnectionProvider extends PetStoreConnectionProvider<PetStoreClient>
    implements PoolingConnectionProvider<PetStoreClient> {

  public static int TIMES_CONNECTED = 0;

  @Optional
  @Parameter
  protected boolean forceConnectionError = false;

  @Override
  public PetStoreClient connect() throws ConnectionException {
    if (forceConnectionError) {
      throw new ConnectionException("Connection failure", new Exception());
    }
    TIMES_CONNECTED++;
    return super.connect();
  }
}
