/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.customos.internal.connection;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import static com.zernova.test.customos.internal.MyOSConnector.VALUES;
import com.zernova.runtime.api.connection.CachedConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;

/**
 * This is a Connection Provider, is executed to obtain new connections when an operation or message source requires.;
 */
@Stereotype(ObjectStoreConnectionStereotype.class)
public class MyOSConnectionProvider implements CachedConnectionProvider<MyOSConnection> {


  public MyOSConnection connect() throws ConnectionException {
    VALUES.clear();
    return new MyOSConnection();
  }

  public void disconnect(MyOSConnection connection) {
    VALUES.clear();
  }

  public ConnectionValidationResult validate(MyOSConnection connection) {
    return success();
  }
}
