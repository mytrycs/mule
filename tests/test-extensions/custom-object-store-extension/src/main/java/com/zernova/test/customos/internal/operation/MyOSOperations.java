/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.customos.internal.operation;

import static com.zernova.test.customos.internal.MyOSConnector.VALUES;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.extension.api.annotation.param.MediaType;

public class MyOSOperations {

  @MediaType("*/*")
  public String retrieve(String key) throws ObjectStoreException {
    return VALUES.get(key).getValue();
  }

}
