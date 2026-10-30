/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.transaction;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.transactionCanOnlyBindToResources;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.internal.transaction.AbstractSingleResourceTransaction;
import com.zernova.runtime.core.internal.transaction.xa.IllegalTransactionStateException;

import java.util.Objects;
import java.util.Optional;

/**
 * Generic single resource transaction for Extensions
 *
 * @since 4.0
 */
public class ExtensionTransaction extends AbstractSingleResourceTransaction {

  private Optional<ExtensionTransactionalResource> boundResource = empty();

  public ExtensionTransaction(String applicationName, NotificationDispatcher notificationFirer) {
    super(applicationName, notificationFirer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void bindResource(Object key, Object resource) throws TransactionException {
    if (!(key instanceof ExtensionTransactionKey) || !(resource instanceof ExtensionTransactionalResource)) {
      throw new IllegalTransactionStateException(transactionCanOnlyBindToResources(format("%s/%s", ExtensionTransactionKey.class
          .getName(), ExtensionTransactionalResource.class.getName())));
    }

    ExtensionTransactionalResource txResource = (ExtensionTransactionalResource) resource;

    boundResource = Optional.of(txResource);
    super.bindResource(key, resource);
    doBegin();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasResource(Object key) {
    return this.key != null && this.key.equals(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getResource(Object key) {
    return Objects.equals(this.key, key) ? this.resource : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doCommit() throws TransactionException {
    if (boundResource.isPresent()) {
      try {
        boundResource.get().commit();
      } catch (Exception e) {
        throw new TransactionException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doRollback() throws TransactionException {
    if (boundResource.isPresent()) {
      try {
        boundResource.get().rollback();
      } catch (Exception e) {
        throw new TransactionException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class getKeyType() {
    return ExtensionTransactionKey.class;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class getResourceType() {
    return ExtensionTransactionalResource.class;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doBegin() throws TransactionException {
    try {
      if (boundResource.isPresent()) {
        boundResource.get().begin();
      }
    } catch (Exception e) {
      throw new TransactionException(e);
    }
  }
}
