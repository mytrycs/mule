/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactional;

import static java.lang.System.identityHashCode;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.api.util.Preconditions.checkState;
import static com.zernova.runtime.extension.api.error.ZernovaErrors.CONNECTIVITY;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.exception.ModuleException;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;
import com.zernova.runtime.extension.api.tx.OperationTransactionalAction;
import com.zernova.test.transactional.connection.TestTransactionalConnection;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionalOperations {

  private AtomicInteger connectionExceptions = new AtomicInteger(0);
  public static Integer getPageCalls = 0;

  @OutputResolver(output = TransactionalMetadataResolver.class)
  public TestTransactionalConnection getConnection(@Connection TestTransactionalConnection connection) {
    return connection;
  }

  public void verifyNoTransaction(@Connection TestTransactionalConnection connection) {
    checkState(!connection.isTransactionBegun(), "transaction begun with no reason");
  }

  public void verifyTransactionBegun(@com.zernova.sdk.api.annotation.param.Connection TestTransactionalConnection connection) {
    checkState(connection.isTransactionBegun(), "transaction not begun");
  }

  public void verifyTransactionCommited(@Connection TestTransactionalConnection connection) {
    checkState(connection.isTransactionCommited(), "transaction not committed");
  }

  public void verifyTransactionRolledback(@Connection TestTransactionalConnection connection) {
    checkState(connection.isTransactionRolledback(), "transaction not rolled back");
  }

  public void verifySameConnection(@Connection TestTransactionalConnection connection,
                                   @Content TestTransactionalConnection transactionalConnection) {
    checkArgument(transactionalConnection != null, "The transactionalConnection can't be null");
    checkState(connection.getConnectionId() == transactionalConnection.getConnectionId(), "The connection is not the same");
  }

  public OperationTransactionalAction injectTransactionalAction(@Connection TestTransactionalConnection connection,
                                                                OperationTransactionalAction action) {
    return action;
  }

  public com.zernova.sdk.api.tx.OperationTransactionalAction sdkInjectTransactionalAction(@Connection TestTransactionalConnection connection,
                                                                                       com.zernova.sdk.api.tx.OperationTransactionalAction action) {
    return action;
  }

  public PagingProvider<TestTransactionalConnection, Integer> pagedTransactionalOperation() throws Exception {
    return new PagingProvider<TestTransactionalConnection, Integer>() {

      private static final int SIZE = 2;
      private int count = 0;
      private int timesClosed = 0;

      @Override
      public List<Integer> getPage(TestTransactionalConnection connection) {
        return count++ < SIZE ? asList(identityHashCode(connection)) : emptyList();
      }

      @Override
      public Optional<Integer> getTotalResults(TestTransactionalConnection connection) {
        return of(SIZE);
      }

      @Override
      public void close(TestTransactionalConnection connection) throws ZernovaException {
        timesClosed++;
        if (timesClosed > 1) {
          throw new RuntimeException("Expected to be closed only once but was called twice");
        }
      }

      @Override
      public boolean useStickyConnections() {
        return false;
      }
    };
  }

  public PagingProvider<TestTransactionalConnection, Integer> failingPagedTransactionalOperation(Integer failOn)
      throws Exception {
    return new PagingProvider<TestTransactionalConnection, Integer>() {

      private static final int SIZE = 2;
      private int count = 0;

      @Override
      public List<Integer> getPage(TestTransactionalConnection connection) {
        getPageCalls++;
        if (getPageCalls == failOn) {
          throw new ModuleException(CONNECTIVITY, new ConnectionException("Failed to retrieve Page"));
        }
        return count++ < SIZE ? asList(identityHashCode(connection)) : emptyList();
      }

      @Override
      public Optional<Integer> getTotalResults(TestTransactionalConnection connection) {
        return empty();
      }

      @Override
      public void close(TestTransactionalConnection connection) throws ZernovaException {}

      @Override
      public boolean useStickyConnections() {
        return false;
      }
    };
  }

  public PagingProvider<TestTransactionalConnection, Integer> stickyFailingPagedTransactionalOperation(Integer failOn)
      throws Exception {
    return new PagingProvider<TestTransactionalConnection, Integer>() {

      private static final int SIZE = 2;
      private int count = 0;

      @Override
      public List<Integer> getPage(TestTransactionalConnection connection) {
        getPageCalls++;
        if (getPageCalls == failOn) {
          throw new ModuleException(CONNECTIVITY, new ConnectionException("Failed to retrieve Page"));
        }
        return count++ < SIZE ? asList(identityHashCode(connection)) : emptyList();
      }

      @Override
      public Optional<Integer> getTotalResults(TestTransactionalConnection connection) {
        return empty();
      }

      @Override
      public void close(TestTransactionalConnection connection) throws ZernovaException {}

      @Override
      public boolean useStickyConnections() {
        return true;
      }
    };
  }

  public void fail() {
    throw new RuntimeException("you better rollback!");
  }

  public void connectionException(@Connection TestTransactionalConnection connection) throws ConnectionException {
    throw new ConnectionException(String.valueOf(connectionExceptions.incrementAndGet()));
  }
}
