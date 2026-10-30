/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import static java.util.Collections.emptyList;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.internal.execution.NotificationFunction;
import com.zernova.runtime.extension.api.notification.NotificationActionDefinition;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.runtime.extension.api.tx.TransactionHandle;
import com.zernova.runtime.module.extension.internal.runtime.source.SourceCallbackContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.transaction.legacy.LegacyTransactionHandle;
import com.zernova.sdk.api.runtime.source.DistributedTraceContextManager;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import java.util.List;
import java.util.Optional;

/**
 * Adapts a {@link SourceCallbackContext} into a legacy
 * {@link com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext}
 *
 * @since 4.4.0
 */
public class LegacySourceCallbackContextAdapter implements AugmentedLegacySourceCallbackContext {

  private final com.zernova.sdk.api.runtime.source.SourceCallbackContext delegate;

  public LegacySourceCallbackContextAdapter(com.zernova.sdk.api.runtime.source.SourceCallbackContext delegate) {
    this.delegate = delegate;
  }

  @Override
  public TransactionHandle bindConnection(Object connection) throws ConnectionException, TransactionException {
    return new LegacyTransactionHandle(delegate.bindConnection(connection));
  }

  @Override
  public <T> T getConnection() throws IllegalStateException {
    return delegate.getConnection();
  }

  @Override
  public TransactionHandle getTransactionHandle() {
    return new LegacyTransactionHandle(delegate.getTransactionHandle());
  }

  @Override
  public boolean hasVariable(String variableName) {
    return delegate.hasVariable(variableName);
  }

  @Override
  public <T> Optional<T> getVariable(String variableName) {
    return delegate.getVariable(variableName);
  }

  @Override
  public void addVariable(String variableName, Object value) {
    delegate.addVariable(variableName, value);
  }

  @Override
  public void setCorrelationId(String correlationId) {
    delegate.setCorrelationId(correlationId);
  }

  @Override
  public Optional<String> getCorrelationId() {
    return delegate.getCorrelationId();
  }

  @Override
  public <T, A> SourceCallback<T, A> getSourceCallback() {
    return new LegacySourceCallbackAdapter<>(delegate.getSourceCallback());
  }

  @Override
  public void fireOnHandle(NotificationActionDefinition<?> action, TypedValue<?> data) {
    delegate.fireOnHandle(action, data);
  }

  @Override
  public void releaseConnection() {
    if (delegate instanceof SourceCallbackContextAdapter) {
      ((SourceCallbackContextAdapter) delegate).releaseConnection();
    }
  }

  @Override
  public void dispatched() {
    if (delegate instanceof SourceCallbackContextAdapter) {
      ((SourceCallbackContextAdapter) delegate).dispatched();
    }
  }

  @Override
  public List<NotificationFunction> getNotificationsFunctions() {
    if (delegate instanceof SourceCallbackContextAdapter) {
      return ((SourceCallbackContextAdapter) delegate).getNotificationsFunctions();
    }
    return emptyList();
  }

  public DistributedTraceContextManager getDistributedSourceTraceContext() {
    return delegate.getDistributedSourceTraceContext();
  }
}
