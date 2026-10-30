/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.transaction;

import static javax.transaction.xa.XAResource.TMSUCCESS;

import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.tx.ZernovaXaObject;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.config.i18n.CoreMessages;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.internal.transaction.XaTransaction;
import com.zernova.runtime.core.internal.transaction.xa.IllegalTransactionStateException;
import com.zernova.sdk.api.connectivity.XATransactionalConnection;

import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialization of {@link ExtensionTransactionalResource} capable of joining XA transactions.
 *
 * @param <T> the generic type of the {@link XATransactionalConnection} that will join the transaction
 * @since 4.0
 */
public class XAExtensionTransactionalResource<T extends XATransactionalConnection> extends ExtensionTransactionalResource<T>
    implements ZernovaXaObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(XAExtensionTransactionalResource.class);

  private XAResource enlistedXAResource;

  private volatile boolean reuseObject = false;

  /**
   * {@inheritDoc}
   */
  public XAExtensionTransactionalResource(T connection, ConnectionHandler<T> connectionHandler, Transaction transaction) {
    super(connection, connectionHandler, transaction);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean enlist() throws TransactionException {
    XaTransaction transaction = getTransaction();

    synchronized (this) {
      if (!isEnlisted()) {
        final XAResource xaResource = getConnection().getXAResource();
        boolean wasAbleToEnlist = transaction.enlistResource(xaResource);
        if (wasAbleToEnlist) {
          enlistedXAResource = xaResource;
        }
      }
    }

    return isEnlisted();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean delist() throws Exception {
    if (!isEnlisted()) {
      return false;
    }

    XaTransaction transaction = getTransaction();

    synchronized (this) {
      if (isEnlisted()) {
        boolean wasAbleToDelist = transaction.delistResource(enlistedXAResource, TMSUCCESS);
        if (wasAbleToDelist) {
          enlistedXAResource = null;
        }
      }
      return !isEnlisted();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws Exception {
    try {
      getConnectionHandler().release();
    } catch (Exception e) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Exception while explicitly closing the xaConnection (some providers require this). "
            + "The exception will be ignored and only logged: " + e.getMessage(), e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isReuseObject() {
    return reuseObject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setReuseObject(boolean reuseObject) {
    this.reuseObject = reuseObject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getTargetObject() {
    return getConnection();
  }

  private boolean isEnlisted() {
    synchronized (this) {
      return enlistedXAResource != null;
    }
  }

  private XaTransaction getTransaction() throws TransactionException {
    Transaction transaction = TransactionCoordination.getInstance().getTransaction();
    if (transaction == null) {
      throw new IllegalTransactionStateException(CoreMessages.noMuleTransactionAvailable());
    }
    if (!(transaction instanceof XaTransaction)) {
      throw new IllegalTransactionStateException(CoreMessages.notMuleXaTransaction(transaction));
    }

    return (XaTransaction) transaction;
  }

}
