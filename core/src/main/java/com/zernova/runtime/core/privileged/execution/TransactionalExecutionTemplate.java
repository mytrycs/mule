/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.execution;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.execution.ExecutionCallback;
import com.zernova.runtime.core.api.execution.ExecutionTemplate;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.execution.BeginAndResolveTransactionInterceptor;
import com.zernova.runtime.core.internal.execution.ExecuteCallbackInterceptor;
import com.zernova.runtime.core.internal.execution.ExecutionContext;
import com.zernova.runtime.core.internal.execution.ExecutionInterceptor;
import com.zernova.runtime.core.internal.execution.IsolateCurrentTransactionInterceptor;
import com.zernova.runtime.core.internal.execution.SuspendXaTransactionInterceptor;
import com.zernova.runtime.core.internal.execution.ValidateTransactionalStateInterceptor;
import com.zernova.runtime.core.internal.execution.compatibility.ResolvePreviousTransactionInterceptor;
import com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.runtime.core.privileged.transaction.TransactionFactory;

/**
 * ExecutionTemplate created should be used on a MessageProcessor that are previously wrapper by
 * {@link TransactionalExecutionTemplate} should be used when:
 * <ul>
 * <li>An outbound endpoint is called.</li>
 * <li>An outbound router is called.</li>
 * <li>Other MessageProcessor able to manage transactions is called.</li>
 * </ul>
 * Any Instance of TransactionTemplate created by this method will:
 * <ul>
 * <li>Resolve non xa transactions created before it if the TransactionConfig action requires.</li>
 * <li>Suspend-Resume xa transaction created before it if the TransactionConfig action requires it.</li>
 * <li>Start a transaction if required by TransactionConfig action.</li>
 * <li><Resolve transaction if was started by this TransactionTemplate.</li>
 * </ul>
 *
 */
public final class TransactionalExecutionTemplate<T> implements ExecutionTemplate<T> {

  private ExecutionInterceptor<T> executionInterceptor;

  private TransactionalExecutionTemplate(String applicationName, NotificationDispatcher notificationDispatcher,
                                         TransactionConfig transactionConfig) {
    this(applicationName, notificationDispatcher, transactionConfig, true, false, true);
  }

  private TransactionalExecutionTemplate(String applicationName, NotificationDispatcher notificationDispatcher,
                                         TransactionConfig transactionConfig,
                                         boolean resolveAnyTransaction, boolean resolvePreviousTx, boolean errorAtTimeout) {
    if (transactionConfig == null) {
      transactionConfig = new ZernovaTransactionConfig();
    }
    final boolean processTransactionOnException = true;
    ExecutionInterceptor<T> tempExecutionInterceptor = new ExecuteCallbackInterceptor<>();
    tempExecutionInterceptor = new BeginAndResolveTransactionInterceptor<>(tempExecutionInterceptor,
                                                                           transactionConfig,
                                                                           applicationName,
                                                                           notificationDispatcher,
                                                                           processTransactionOnException,
                                                                           resolveAnyTransaction, errorAtTimeout);
    if (resolvePreviousTx) {
      tempExecutionInterceptor = new ResolvePreviousTransactionInterceptor<>(tempExecutionInterceptor, transactionConfig);
    }
    tempExecutionInterceptor = new SuspendXaTransactionInterceptor<>(tempExecutionInterceptor, transactionConfig,
                                                                     processTransactionOnException);
    tempExecutionInterceptor =
        new ValidateTransactionalStateInterceptor<>(tempExecutionInterceptor, transactionConfig, resolvePreviousTx);
    this.executionInterceptor = new IsolateCurrentTransactionInterceptor<>(tempExecutionInterceptor, transactionConfig);
  }

  /**
   * Creates a ExecutionTemplate that will manage transactional context according to the given txAction
   */
  public static <T> TransactionalExecutionTemplate<T> createTransactionalExecutionTemplate(Registry registry,
                                                                                           byte txAction,
                                                                                           TransactionFactory factory) {
    final var muleContext = registry.lookupByType(ZernovaContext.class).orElseThrow();

    ZernovaTransactionConfig transactionConfig = new ZernovaTransactionConfig(txAction);
    transactionConfig.setFactory(factory);

    return new TransactionalExecutionTemplate<>(getApplicationName(muleContext),
                                                getNotificationDispatcher((ZernovaContextWithRegistry) muleContext),
                                                transactionConfig);
  }

  /**
   * Creates a ExecutionTemplate that will manage transactional context according to configured TransactionConfig
   */
  public static <T> TransactionalExecutionTemplate<T> createTransactionalExecutionTemplate(ZernovaConfiguration muleConfiguration,
                                                                                           NotificationDispatcher notificationDispatcher,
                                                                                           TransactionConfig transactionConfig) {
    return new TransactionalExecutionTemplate<>(getApplicationName(muleConfiguration),
                                                notificationDispatcher,
                                                transactionConfig);
  }

  private static NotificationDispatcher getNotificationDispatcher(ZernovaContextWithRegistry muleContext) {
    try {
      return muleContext.getRegistry().lookupObject(NotificationDispatcher.class);
    } catch (RegistrationException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  private static String getApplicationName(ZernovaContext muleContext) {
    return muleContext.getConfiguration().getId();
  }

  private static String getApplicationName(ZernovaConfiguration muleConfiguration) {
    return muleConfiguration.getId();
  }

  /**
   * Creates a TransactionalExecutionTemplate for inner scopes within a flow
   *
   * @return <T>
   */
  public static <T> TransactionalExecutionTemplate<T> createScopeTransactionalExecutionTemplate(ZernovaConfiguration muleConfiguration,
                                                                                                NotificationDispatcher notificationDispatcher,
                                                                                                TransactionConfig transactionConfig) {
    return new TransactionalExecutionTemplate<>(getApplicationName(muleConfiguration),
                                                notificationDispatcher,
                                                transactionConfig, false, false, true);
  }

  /**
   * Creates a TransactionalExecutionTemplate for inner scopes within a flow
   *
   * @return <T>
   */
  public static <T> TransactionalExecutionTemplate<T> createScopeTransactionalExecutionTemplate(ZernovaConfiguration muleConfiguration,
                                                                                                NotificationDispatcher notificationDispatcher,
                                                                                                TransactionConfig transactionConfig,
                                                                                                boolean errorAfterTimeout) {
    return new TransactionalExecutionTemplate<>(getApplicationName(muleConfiguration),
                                                notificationDispatcher,
                                                transactionConfig, false, false, errorAfterTimeout);
  }

  @Override
  public T execute(ExecutionCallback<T> executionCallback) throws Exception {
    return executionInterceptor.execute(executionCallback, new ExecutionContext());
  }
}
