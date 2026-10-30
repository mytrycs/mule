/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity;

import static com.zernova.runtime.core.api.config.ZernovaDeploymentProperties.MULE_LAZY_CONNECTIONS_DEPLOYMENT_PROPERTY;
import static com.zernova.runtime.extension.api.util.NameUtils.getComponentModelTypeName;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.profiling.InternalProfilingService;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.runtime.extension.api.connectivity.TransactionalConnection;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.EventedExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.transaction.ExtensionTransactionKey;
import com.zernova.runtime.module.extension.internal.runtime.transaction.TransactionBindingDelegate;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.runtime.tracer.api.EventTracer;

import java.util.Optional;

import jakarta.inject.Inject;

/**
 * A bridge between the execution of a {@link ComponentModel} and the {@link ConnectionManager} which provides the connections
 * that it needs.
 * <p>
 * It handles connection provisioning and transaction support
 *
 * @since 4.0
 */
public class ExtensionConnectionSupplier {

  @Inject
  private ConnectionManager connectionManager;

  @Inject
  InternalProfilingService internalProfilingService;

  private boolean lazyConnections;

  /**
   * Returns the connection to be used with the {@code operationContext}.
   * <p>
   * It accounts for the possibility of the returned connection joining/belonging to an active transaction
   *
   * @param executionContext          an {@link ExecutionContextAdapter}
   * @param operationConnectionTracer a {@link ComponentTracer} that will be used to trace the connection obtetnion.
   * @return a {@link ConnectionHandler}
   * @throws ConnectionException  if connection could not be obtained
   * @throws TransactionException if something is wrong with the transaction
   */
  public ConnectionHandler getConnection(ExecutionContextAdapter<? extends ComponentModel> executionContext,
                                         ComponentTracer<CoreEvent> operationConnectionTracer)
      throws ConnectionException, TransactionException {

    // Tracing cannot break application logic, so we avoid possible tracing related NPEs.
    if (operationConnectionTracer == null) {
      return getConnectionHandler(executionContext);
    }

    ConnectionHandler<?> connectionHandler;
    if (lazyConnections) {
      connectionHandler =
          new TracedLazyConnection(getConnectionHandler(executionContext), internalProfilingService.getCoreEventTracer(),
                                   operationConnectionTracer, executionContext);
    } else {
      operationConnectionTracer.startSpan(executionContext.getEvent());
      try {
        connectionHandler = getConnectionHandler(executionContext);
      } finally {
        operationConnectionTracer.endCurrentSpan(executionContext.getEvent());
      }
    }
    return connectionHandler;
  }

  private ConnectionHandler<?> getConnectionHandler(ExecutionContextAdapter<? extends ComponentModel> executionContext)
      throws ConnectionException, TransactionException {
    return executionContext.getTransactionConfig().isPresent()
        ? getTransactedConnectionHandler(executionContext, executionContext.getTransactionConfig().get())
        : getTransactionlessConnectionHandler(executionContext);
  }

  private <T extends TransactionalConnection> ConnectionHandler<T> getTransactedConnectionHandler(
                                                                                                  ExecutionContextAdapter<? extends ComponentModel> executionContext,
                                                                                                  TransactionConfig transactionConfig)
      throws ConnectionException, TransactionException {

    if (!transactionConfig.isTransacted()) {
      return getTransactionlessConnectionHandler(executionContext);
    }

    ExtensionModel extensionModel = executionContext.getExtensionModel();
    ComponentModel componentModel = executionContext.getComponentModel();

    ConfigurationInstance configuration = executionContext.getConfiguration()
        .orElseThrow(() -> new IllegalStateException(format(
                                                            "%s '%s' of extension '%s' cannot participate in a transaction because it doesn't have a config",
                                                            getComponentModelTypeName(componentModel),
                                                            componentModel.getName(),
                                                            extensionModel.getName())));


    final ExtensionTransactionKey txKey = new ExtensionTransactionKey(configuration);

    TransactionBindingDelegate transactionBindingDelegate = new TransactionBindingDelegate(extensionModel, componentModel);
    return transactionBindingDelegate.getBoundResource(lazyConnections, txKey,
                                                       () -> getTransactionlessConnectionHandler(executionContext));
  }

  private <T> ConnectionHandler<T> getTransactionlessConnectionHandler(ExecutionContext executionContext)
      throws ConnectionException {


    final Optional<ConfigurationInstance> configuration = executionContext.getConfiguration();
    Optional<ConnectionProvider> connectionProvider = configuration.flatMap(ConfigurationInstance::getConnectionProvider);

    if (!connectionProvider.isPresent()) {
      String configRef = configuration
          .map(config -> format("with config '%s' which is not associated to a connection provider", config.getName()))
          .orElse("without a config");

      throw new IllegalStateException(format("%s '%s' of extension '%s' requires a connection but was executed %s",
                                             getComponentModelTypeName(executionContext.getComponentModel()),
                                             executionContext.getComponentModel().getName(),
                                             executionContext.getExtensionModel().getName(),
                                             configRef));
    }

    return connectionManager.getConnection(configuration.get().getValue());
  }

  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    this.lazyConnections =
        parseBoolean(muleContext.getDeploymentProperties().getProperty(MULE_LAZY_CONNECTIONS_DEPLOYMENT_PROPERTY, "false"));
  }

  /**
   * A wrapper around a lazy {@link ConnectionHandler} that generates tracing spans when the connection is actually established.
   *
   * @param <T> The generic type of the connection being handled.
   */
  private static class TracedLazyConnection<T> implements ConnectionHandler<T> {

    private final ConnectionHandler<T> lazyConnectionHandler;
    private final EventTracer<CoreEvent> eventTracer;
    private final CoreEvent tracedEvent;
    private final ComponentTracer componentTracer;

    public TracedLazyConnection(ConnectionHandler<T> lazyConnectionHandler, EventTracer<CoreEvent> eventTracer,
                                ComponentTracer componentTracer, EventedExecutionContext<?> executionContext) {
      this.lazyConnectionHandler = lazyConnectionHandler;
      this.eventTracer = eventTracer;
      this.componentTracer = componentTracer;
      this.tracedEvent = executionContext.getEvent();
    }

    @Override
    public T getConnection() throws ConnectionException {
      componentTracer.startSpan(tracedEvent);
      try {
        return lazyConnectionHandler.getConnection();
      } finally {
        eventTracer.endCurrentSpan(tracedEvent);
      }
    }

    @Override
    public void release() {
      lazyConnectionHandler.release();
    }

    @Override
    public void invalidate() {
      lazyConnectionHandler.invalidate();
    }
  }
}
