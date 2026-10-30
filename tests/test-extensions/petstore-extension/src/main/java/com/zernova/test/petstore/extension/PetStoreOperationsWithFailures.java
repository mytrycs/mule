/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableList;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.extension.api.annotation.Ignore;
import com.zernova.runtime.extension.api.annotation.param.Connection;

import java.util.ArrayList;
import java.util.List;

public class PetStoreOperationsWithFailures extends PetStoreOperations {

  public static final String CONNECTION_FAIL = "Connection fail";

  /**
   * The threads on which a connection was executed.
   */
  private static List<Thread> connectionThreads = new ArrayList<>();

  @Ignore
  public static List<Thread> getConnectionThreads() {
    return unmodifiableList(connectionThreads);
  }

  public static void resetConnectionThreads() {
    connectionThreads.clear();
  }

  public Integer failConnection(@Connection PetStoreClient client) throws ConnectionException {
    connectionThreads.add(currentThread());
    throw new ConnectionException(CONNECTION_FAIL);
  }

  public Integer failOperationWithException(@Connection PetStoreClient client) throws Exception {
    connectionThreads.add(currentThread());
    throw new Exception(CONNECTION_FAIL);
  }

  public Integer failOperationWithThrowable(@Connection PetStoreClient client) throws Throwable {
    connectionThreads.add(currentThread());
    throw new Throwable(CONNECTION_FAIL);
  }
}
