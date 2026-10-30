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
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.runtime.module.extension.internal.runtime.notification.legacy.LegacyNotificationActionDefinitionAdapter;
import com.zernova.runtime.module.extension.internal.runtime.source.SourceCallbackContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.source.trace.SourceDistributedTraceContextManager;
import com.zernova.sdk.api.notification.NotificationActionDefinition;
import com.zernova.sdk.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.runtime.source.DistributedTraceContextManager;
import com.zernova.sdk.api.tx.TransactionHandle;

import java.util.List;
import java.util.Optional;

/**
 * Adapts a legacy {@link SourceCallbackContext} into a {@link SourceCallbackContextAdapter}
 *
 * @since 4.4.0
 */
public class SdkSourceCallBackContextAdapter implements SourceCallbackContextAdapter {

  private final SourceCallbackContext delegate;
  private DistributedTraceContextManager distributedSourceTraceContext = new SourceDistributedTraceContextManager();

  public SdkSourceCallBackContextAdapter(SourceCallbackContext delegate) {
    this.delegate = delegate;
  }

  @Override
  public TransactionHandle bindConnection(Object o) throws ConnectionException, TransactionException {
    return delegate.bindConnection(o);
  }

  @Override
  public <T> T getConnection() throws IllegalStateException {
    return delegate.getConnection();
  }

  @Override
  public TransactionHandle getTransactionHandle() {
    return delegate.getTransactionHandle();
  }

  @Override
  public boolean hasVariable(String s) {
    return delegate.hasVariable(s);
  }

  @Override
  public <T> Optional<T> getVariable(String s) {
    return delegate.getVariable(s);
  }

  @Override
  public void addVariable(String s, Object o) {
    delegate.addVariable(s, o);
  }

  @Override
  public void setCorrelationId(String s) {
    delegate.setCorrelationId(s);
  }

  @Override
  public Optional<String> getCorrelationId() {
    return delegate.getCorrelationId();
  }

  @Override
  public <T, A> SourceCallback<T, A> getSourceCallback() {
    return new SdkSourceCallbackAdapter<>(delegate.getSourceCallback());
  }

  @Override
  public void fireOnHandle(NotificationActionDefinition<?> notificationActionDefinition, TypedValue<?> typedValue) {
    delegate.fireOnHandle(new LegacyNotificationActionDefinitionAdapter(notificationActionDefinition), typedValue);
  }

  @Override
  public DistributedTraceContextManager getDistributedSourceTraceContext() {
    if (delegate instanceof LegacySourceCallbackContextAdapter) {
      return ((LegacySourceCallbackContextAdapter) delegate).getDistributedSourceTraceContext();
    }
    return distributedSourceTraceContext;
  }

  @Override
  public void releaseConnection() {
    if (delegate instanceof AugmentedLegacySourceCallbackContext) {
      ((AugmentedLegacySourceCallbackContext) delegate).releaseConnection();
    }
  }

  @Override
  public void dispatched() {
    if (delegate instanceof AugmentedLegacySourceCallbackContext) {
      ((AugmentedLegacySourceCallbackContext) delegate).dispatched();
    }
  }

  @Override
  public List<NotificationFunction> getNotificationsFunctions() {
    if (delegate instanceof AugmentedLegacySourceCallbackContext) {
      return ((AugmentedLegacySourceCallbackContext) delegate).getNotificationsFunctions();
    }
    return emptyList();
  }
}
