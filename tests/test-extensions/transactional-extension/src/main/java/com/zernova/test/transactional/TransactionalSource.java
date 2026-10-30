/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactional;

import static java.util.concurrent.Executors.newFixedThreadPool;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.api.tx.TransactionType;
import com.zernova.runtime.core.api.util.concurrent.NamedThreadFactory;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.execution.OnTerminate;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.connectivity.XATransactionalConnection;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.runtime.extension.api.tx.SourceTransactionalAction;
import com.zernova.runtime.extension.api.tx.TransactionHandle;
import com.zernova.test.transactional.connection.DummyXaResource;
import com.zernova.test.transactional.connection.TestTransactionalConnection;
import com.zernova.test.transactional.connection.TestXaTransactionalConnection;

import java.util.concurrent.ExecutorService;

@MetadataScope(outputResolver = TransactionalMetadataResolver.class)
public class TransactionalSource extends Source<TestTransactionalConnection, Object> {

  private static final String IS_XA = "isXa";

  public static Boolean isSuccess;
  public static DummyXaResource xaResource;

  @Parameter
  TransactionType txType;

  private SourceTransactionalAction txAction;

  @Connection
  private ConnectionProvider<TestTransactionalConnection> connectionProvider;

  private ExecutorService connectExecutor;

  public TransactionalSource() {
    isSuccess = null;
    xaResource = null;
  }

  @Override
  public void onStart(SourceCallback<TestTransactionalConnection, Object> sourceCallback) {
    connectExecutor = newFixedThreadPool(1, new NamedThreadFactory(TransactionalSource.class.getName()));
    connectExecutor.execute(() -> {
      SourceCallbackContext ctx = sourceCallback.createContext();
      TransactionHandle txHandle = null;
      try {
        TestTransactionalConnection connection = connectionProvider.connect();

        boolean isXa = false;
        if (connection instanceof XATransactionalConnection) {
          isXa = true;
          xaResource = (DummyXaResource) ((XATransactionalConnection) connection).getXAResource();
        }

        ctx.addVariable(IS_XA, isXa);
        txHandle = ctx.bindConnection(connection);
        sourceCallback.handle(Result.<TestTransactionalConnection, Object>builder().output(connection).build(), ctx);
      } catch (ConnectionException e) {
        sourceCallback.onConnectionException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        try {
          if (txHandle != null) {
            txHandle.commit();
          }
        } catch (TransactionException e) {
          try {
            txHandle.rollback();
          } catch (TransactionException e1) {
            final RuntimeException rollbackException = new RuntimeException(e1);
            rollbackException.addSuppressed(e);
            throw rollbackException;
          }
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Override
  public void onStop() {
    connectExecutor.shutdownNow();
    connectExecutor = null;
  }

  @OnSuccess
  public void onSuccess(com.zernova.sdk.api.runtime.source.SourceCallbackContext ctx)
      throws TransactionException {
    ctx.getTransactionHandle().commit();
    isSuccess = true;
  }

  @OnError
  public void onError(SourceCallbackContext ctx)
      throws TransactionException {
    ctx.getTransactionHandle().rollback();
    isSuccess = false;
  }

  @OnTerminate
  public void onTerminate(com.zernova.sdk.api.runtime.source.SourceCallbackContext ctx) {
    Boolean isXa = (Boolean) ctx.getVariable(IS_XA).get();
    if (isXa) {
      TestXaTransactionalConnection connection = ctx.getConnection();
      DummyXaResource xaResource = (DummyXaResource) connection.getXAResource();
    }
  }
}
